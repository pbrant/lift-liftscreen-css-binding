/*
 * Copyright 2010-2011 WorldWide Conferencing, LLC
 * Copyright 2011 Wisconsin Court System
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.wicourts.jdash2.form

import net.liftweb.http._
import js.JsCmd
import net.liftweb.util._
import Helpers._
import net.liftweb.common.{Full, Box}
import net.liftweb.http.{SHtml, AbstractScreen, ScreenFieldInfo}
import xml._

trait CssBoundScreen extends ScreenWizardRendered {
  self: AbstractScreen =>

  @volatile var cssBindingInfo:Map[FieldIdentifier, CssBindingInfo] = Map[FieldIdentifier, CssBindingInfo]()

  def formName:String

  def labelSuffix:NodeSeq = Text(":")

  protected lazy val cssClassBinding = new CssClassBinding

  protected def bindToId(field:Field, cssFieldName:String):Field = bindToId(field, cssFieldName, Default)

  protected def bindToId(field:Field, cssFieldName:String, cssBindingStyle:CssBindingStyle):Field = {
    cssBindingInfo += ((field, new CssBindingInfo(cssFieldName, cssBindingStyle)))
    field
  }

  object CssBindingUtils {
    def sel(f:CssClassBinding => String, sel:String) = sel format (f(cssClassBinding))
    def replace(f:CssClassBinding => String) = ".%s" format (f(cssClassBinding))
    def replaceChildren(f:CssClassBinding => String) = ".%s *" format (f(cssClassBinding))

    def remove(f:CssClassBinding => String) = ".%s".format(f(cssClassBinding)) #> NodeSeq.Empty

    // Separate names (vs. overloaded methods) to help the type inferencer out
    def nsSetChildren(f:CssClassBinding=>String, value:NodeSeq) = replaceChildren(f) #> value
    def funcSetChildren(f:CssClassBinding=>String, value:NodeSeq=>NodeSeq) = replaceChildren(f) #> value
    def optSetChildren(f:CssClassBinding=>String, value:Box[NodeSeq]) = replaceChildren(f) #> value

    def nsReplace(f:CssClassBinding=>String, value:NodeSeq) = replace(f) #> value
    def funcReplace(f:CssClassBinding=>String, value:NodeSeq=>NodeSeq) = replace(f) #> value
    def optReplace(f:CssClassBinding=>String, value:Box[NodeSeq]) = replace(f) #> value

    def updateAttrs(metaData: MetaData): NodeSeq=>NodeSeq = {
      case e:Elem => e % metaData
    }

    def update(f:CssClassBinding => String, metaData: MetaData) =
      ".%s".format(f(cssClassBinding)) #> updateAttrs(metaData)
  }

  sealed abstract class CssBindingStyle
  case object Template extends CssBindingStyle
  case object Self extends CssBindingStyle
  case object Default extends CssBindingStyle

  override protected def renderAll(currentScreenNumber: Box[NodeSeq],
                          screenCount: Box[NodeSeq],
                          wizardTop: Box[Elem],
                          screenTop: Box[Elem],
                          fields: List[ScreenFieldInfo],
                          prev: Box[Elem],
                          cancel: Box[Elem],
                          next: Box[Elem],
                          finish: Box[Elem],
                          screenBottom: Box[Elem],
                          wizardBottom: Box[Elem],
                          nextId: (String, () => JsCmd),
                          prevId: Box[(String, () => JsCmd)],
                          cancelId: (String, () => JsCmd),
                          theScreen: AbstractScreen,
                          ajax_? : Boolean): NodeSeq = {

    import CssBindingUtils._

    val notices: List[(NoticeType.Value, NodeSeq, Box[String])] = S.getAllNotices

    def fieldsWithStyle(style:CssBindingStyle, includeMissing:Boolean) = fields filter (field =>
      cssBindingInfo.get(field.field) match {
        case None => includeMissing
        case Some(info) => info.cssBindingStyle == style
      }
    )

    def bindingInfoWithFields(style:CssBindingStyle) =
      for (field <- fields;
           bindingInfo <- cssBindingInfo.get(field.field) if bindingInfo.cssBindingStyle == style)
      yield (bindingInfo, field)

    def templateFields: List[CssBindFunc] = List(sel(_.fieldContainer, ".%s") #> (fieldsWithStyle(Template, true) map (field => bindField(field))))

    def selfFields: List[CssBindFunc] =
      for ((bindingInfo, field) <- bindingInfoWithFields(Self))
      yield bindingInfo.selector #> bindField(field)

    def defaultFields: List[CssBindFunc] =
      for ((bindingInfo, field) <- bindingInfoWithFields(Default))
      yield bindingInfo.childSelector #> bindField(field)(stripRoot(defaultFieldNodeSeq))

    def bindFields: CssBindFunc = List(templateFields, selfFields, defaultFields).flatten.reduceLeft(_ & _)

    def bindField(f:ScreenFieldInfo):CssBindFunc = {
      val theFormEarly = f.input
      val curId = theFormEarly.flatMap(Helpers.findId) or
        f.field.uniqueFieldId openOr Helpers.nextFuncName

      val theForm = theFormEarly.map{
        fe => {
          val f = Helpers.deepEnsureUniqueId(fe)
          val id = Helpers.findBox(f)(_.attribute("id").
            map(_.text).
            filter(_ == curId))
          if (id.isEmpty) {
            Helpers.ensureId(f, curId)
          } else {
            f
          }
        }
      }

      val myNotices = notices.filter(fi => fi._3.isDefined && fi._3 == curId)

      def bindLabel(): CssBindFunc = {
        val basicLabel = sel(_.label, ".%s [for]") #> curId & nsSetChildren(_.label, f.text ++ labelSuffix)
        myNotices match {
          case Nil => basicLabel
          case _ =>
            val maxN = myNotices.map(_._1).sortWith{_.id > _.id}.head // get the maximum type of notice (Error > Warning > Notice)
            val metaData: MetaData = noticeTypeToAttr(theScreen).map(_(maxN)) openOr Null
            basicLabel & update(_.label, metaData)
        }
      }

      def bindForm(): CssBindFunc = replace(_.value) #> theForm

      def bindHelp(): CssBindFunc =
        f.help match {
          case Full(hlp) => nsSetChildren(_.help, hlp)
          case _ => remove(_.help)
        }

      def bindErrors(): CssBindFunc =
        myNotices match {
          case Nil => remove(_.errors)
          case xs => replaceChildren(_.errors) #> xs.map { case(noticeType, msg, _) =>
            val metaData: MetaData = noticeTypeToAttr(theScreen).map(_(noticeType)) openOr Null
            nsSetChildren(_.error, msg) & update(_.error, metaData)
          }
        }

      bindLabel() & bindForm() & bindHelp() & bindErrors()
    }

    def url = S.uri

    val snapshot = createSnapshot

    def bindErrors: CssBindFunc = notices.filter(_._3.isEmpty) match {
      case Nil => remove(_.globalErrors)
      case xs => replaceChildren(_.globalErrors) #> xs.map { case(noticeType, msg, _) =>
        val metaData: MetaData = noticeTypeToAttr(theScreen).map(_(noticeType)) openOr Null
        nsSetChildren(_.error, msg) & update(_.error, metaData)
      }
    }

    def bindForm(xhtml: NodeSeq): NodeSeq = {
      val ret =
        (<form id={nextId._1} action={url}
               method="post">{S.formGroup(-1)(SHtml.hidden(() =>
          snapshot.restore()))}{bindFields(xhtml)}{
          S.formGroup(4)(
            SHtml.hidden(() =>
            {val res = nextId._2();
              if (!ajax_?) {
                val localSnapshot = createSnapshot
                S.seeOther(url, () => {
                  localSnapshot.restore
                })}
              res
            }))}</form> %
          theScreen.additionalAttributes) ++
          prevId.toList.map{case (id, func) =>
            <form id={id} action={url} method="post">{
              SHtml.hidden(() => {snapshot.restore();
                val res = func();
                if (!ajax_?) {
                  val localSnapshot = createSnapshot;
                  S.seeOther(url, () => localSnapshot.restore)
                }
                res
              })}</form>
          } ++
          <form id={cancelId._1} action={url} method="post">{SHtml.hidden(() => {
            snapshot.restore();
            val res = cancelId._2() // WizardRules.deregisterWizardSession(CurrentSession.is)
            if (!ajax_?) {
              S.seeOther(Referer.get)
            }
            res
          })}</form>

      if (ajax_?) {
        SHtml.makeFormsAjax(ret)
      } else {
        ret
      }
    }

    def bindScreenInfo: CssBindFunc = (currentScreenNumber, screenCount) match {
      case (Full(num), Full(cnt)) =>
        replaceChildren(_.screenInfo) #> (nsSetChildren(_.screenNumber, num) & nsSetChildren(_.totalScreens, cnt))
      case _ => remove(_.screenInfo)
    }

    val bindingFunc:CssBindFunc =
      bindScreenInfo &
      optSetChildren(_.wizardTop, wizardTop) &
      optSetChildren(_.screenTop, screenTop) &
      optSetChildren(_.wizardBottom, wizardBottom) &
      optSetChildren(_.screenBottom, screenBottom) &
      nsReplace(_.prev, prev openOr EntityRef("nbsp")) &
      nsReplace(_.next, ((next or finish) openOr EntityRef("nbsp"))) &
      nsReplace(_.cancel, cancel openOr EntityRef("nbsp")) &
      bindErrors &
      funcSetChildren(_.fields, bindForm _)

    bindingFunc(allTemplate)
  }

  override protected def allTemplateNodeSeq: NodeSeq = {
    <div>
      <div class="screenInfo">
        Page <span class="screenNumber"></span> of <span class="totalScreens"></span>
      </div>
      <div class="wizardTop"></div>
      <div class="screenTop"></div>
      <div class="globalErrors">
        <div class="error"></div>
      </div>
      <div class="fields">
        <table>
          <tr class="fieldContainer">
            <td>
              <label class="label field"></label>
              <span class="help"></span>
              <div class="errors">
                <div class="error"></div>
              </div>
            </td>
            <td><span class="value fieldValue"></span></td>
          </tr>
        </table>
      </div>
      <div>
        <table>
          <tr>
            <td><button class="prev"></button></td>
            <td><button class="cancel"></button></td>
            <td><button class="next"></button> </td>
          </tr>
        </table>
      </div>
      <div class="screenBottom"></div>
      <div class="wizardBottom"></div>
    </div>
  }

  private def stripRoot(n:NodeSeq):NodeSeq =
    n.collect({ case e:Elem => e}).headOption.map(_.child).getOrElse(NodeSeq.Empty)

  def defaultFieldNodeSeq:NodeSeq = NodeSeq.Empty

  class CssBindingInfo(val fieldName:String, val cssBindingStyle:CssBindingStyle) {
    def fieldId = "%s_%s_field" format (formName, fieldName)
    def selector = "#%s" format (fieldId)
    def childSelector = "#%s *" format (fieldId)
    def idSelector = selector + " [id]"
  }

  class CssClassBinding {
    def screenInfo = "screenInfo"
    def wizardTop = "wizardTop"
    def screenTop = "screenTop"
    def globalErrors = "globalErrors"
    def fields = "fields"
    def fieldContainer = "fieldContainer"
    def label = "label"
    def help = "help"
    def errors = "errors"
    def error = "error"
    def value = "value"
    def prev = "prev"
    def cancel = "cancel"
    def next = "next"
    def screenBottom = "screenBottom"
    def wizardBottom = "wizardBottom"
    def screenNumber = "screenNumber"
    def totalScreens = "totalScreens"
  }
}