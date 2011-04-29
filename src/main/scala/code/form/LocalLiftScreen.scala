/*
 * Copyright 2010-2011 WorldWide Conferencing, LLC
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

import xml._
import reflect.Manifest

import net.liftweb.http._

import net.liftweb.http.js._
import JsCmds._

import net.liftweb.util._
import Helpers._

import net.liftweb.common._

trait LocalLiftScreen extends AbstractScreen with StatefulSnippet with ScreenWizardRendered {
  def dispatch = {
    case _ => template => {
      _defaultXml.set(template)
      this.toForm
    }
  }

  /**
   * By default, are all the fields on this screen on the confirm screen?
   */
  def onConfirm_? : Boolean = true

  /**
   * Holds the template passed via the snippet for the duration
   * of the request
   */
  protected val _defaultXml = vendATransientRequestVar[NodeSeq](NodeSeq.Empty)

  /**
   * the NodeSeq passed as a parameter when the snippet was invoked
   */
  protected def defaultXml: NodeSeq = _defaultXml.get

  private val ScreenVars = vendATransientRequestVar[Map[String, (NonCleanAnyVar[_], Any)]](Map())
  private val PrevSnapshot = vendATransientRequestVar[Box[ScreenSnapshot]](Empty)
  protected val Referer = vendAVar[String](calcReferer)
  /**
   * A unique GUID for the form... this allows us to do an Ajax SetHtml
   * to replace the form
   */
  protected val FormGUID = vendAVar[String](Helpers.nextFuncName)

  protected val Ajax_? = vendAVar[Boolean](calcAjax)
  /**
   * What to do when the Screen is done.  By default, will
   * do a redirect back to Whence, but you can change this behavior,
   * for example, put up some other Ajax thing or alternatively,
   * remove the form from the screen.
   */
  protected val AjaxOnDone = vendAVar[JsCmd](calcAjaxOnDone)
  private val FirstTime = vendAVar[Boolean](true)

  protected class ScreenSnapshot(val screenVars: Map[String, (NonCleanAnyVar[_], Any)],
                                 val snapshot: Box[ScreenSnapshot]) extends Snapshot {
    def restore() {
      registerThisSnippet();
      ScreenVars.set(screenVars)
      PrevSnapshot.set(snapshot)
    }
  }

  protected def vendAVar[T](dflt: => T): NonCleanAnyVar[T] = new ScreenVar[T](dflt) {
    override protected def __nameSalt = randomString(20)
  }

  protected def vendATransientRequestVar[T](dflt: => T) = new TransientRequestVar[T](dflt) {
    override protected def __nameSalt = randomString(20)
  }

  protected def createSnapshot = {
    val prev = PrevSnapshot.get
    new ScreenSnapshot(ScreenVars.get, prev)
  }

  /**
   * Keep request-local information around without the nastiness of naming session variables
   * or the type-unsafety of casting the results.
   * RequestVars share their value through the scope of the current HTTP
   * request. They have no value at the beginning of request servicing
   * and their value is discarded at the end of request processing. They
   * are helpful to share values across many snippets.
   *
   * @param dflt - the default value of the session variable
   */
  abstract class ScreenVar[T](dflt: => T) extends NonCleanAnyVar[T](dflt) {
    override protected def findFunc(name: String): Box[T] = ScreenVarHandler.get(name)

    override protected def setFunc(name: String, value: T): Unit = ScreenVarHandler.set(name, this, value)

    override protected def clearFunc(name: String): Unit = ScreenVarHandler.clear(name)

    override protected def wasInitialized(name: String): Boolean = {
      val bn = name + "_inited_?"
      val old: Boolean = ScreenVarHandler.get(bn) openOr false
      ScreenVarHandler.set(bn, this, true)
      old
    }

    override protected def testWasSet(name: String): Boolean = {
      val bn = name + "_inited_?"
      ScreenVarHandler.get(name).isDefined || (ScreenVarHandler.get(bn) openOr false)
    }

    /**
     * Different Vars require different mechanisms for synchronization. This method implements
     * the Var specific synchronization mechanism
     */
    def doSync[F](f: => F): F = f // no sync necessary for RequestVars... always on the same thread
  }


  private object ScreenVarHandler {
    def get[T](name: String): Box[T] =
      ScreenVars.is.get(name).map(_._2.asInstanceOf[T])


    def set[T](name: String, from: ScreenVar[_], value: T): Unit =
      ScreenVars.set(ScreenVars.get + (name -> (from, value)))

    def clear(name: String): Unit =
      ScreenVars.set(ScreenVars.get - name)
  }

  def toForm: NodeSeq = {
    Referer.get // touch to capture the referer
    Ajax_?.get // capture the ajaxiness of these forms
    FormGUID.get

    if (FirstTime) {
      FirstTime.set(false)
      localSetup()

      val localSnapshot = createSnapshot
      // val notices = S.getAllNotices

      // if we're not Ajax,
      if (!ajaxForms_?) {
        S.seeOther(S.uri, () => {
          // S.appendNotices(notices)
          localSnapshot.restore
        })
      }
    }

    val form = renderHtml()
    if (ajaxForms_?) wrapInDiv(form) else form
  }

  protected def renderHtml(): NodeSeq = {
    val finishId = Helpers.nextFuncName
    val cancelId = Helpers.nextFuncName

    val theScreen = this

    val finishButton = theScreen.finishButton %
      ("onclick" ->
        (if (ajaxForms_?) {
          SHtml.makeAjaxCall(LiftRules.jsArtifacts.serialize(finishId)).toJsCmd
        } else {
          "document.getElementById(" + finishId.encJs + ").submit()"
        }))

    val cancelButton: Elem = theScreen.cancelButton %
      ("onclick" ->
        (if (ajaxForms_?) {
          SHtml.makeAjaxCall(LiftRules.jsArtifacts.serialize(cancelId)).toJsCmd
        } else {
          "document.getElementById(" + cancelId.encJs + ").submit()"
        }))

    val url = S.uri

    renderAll(
      Empty, //currentScreenNumber: Box[NodeSeq],
      Empty, //screenCount: Box[NodeSeq],
      Empty, // wizardTop: Box[Elem],
      theScreen.screenTop, //screenTop: Box[Elem],
      theScreen.screenFields.filter(_.shouldDisplay_?).flatMap(f =>
        if (f.show_?) List(ScreenFieldInfo(f, f.displayHtml, f.helpAsHtml, f.toForm)) else Nil), //fields: List[ScreenFieldInfo],
      Empty, // prev: Box[Elem],
      Full(cancelButton), // cancel: Box[Elem],
      Empty, // next: Box[Elem],
      Full(finishButton), //finish: Box[Elem],
      theScreen.screenBottom, // screenBottom: Box[Elem],
      Empty, //wizardBottom: Box[Elem],
      finishId -> doFinish _,
      Empty,
      cancelId -> (() => {redirectBack()}), //cancelId: (String, () => Unit),
      theScreen,
      ajaxForms_?)
  }

  protected def allTemplatePath: List[String] = LiftScreenRules.allTemplatePath.vend

  protected def allTemplate: NodeSeq = {
    val ret = TemplateFinder.
      findAnyTemplate(allTemplatePath) openOr allTemplateNodeSeq

    ret
  }

  /**
   * What additional attributes should be put on the
   */
  protected def formAttrs: MetaData = scala.xml.Null

  protected def finish(): Unit

  protected def doFinish(): JsCmd= {
    validate match {
      case Nil =>
        val snapshot = createSnapshot
        PrevSnapshot.set(Full(snapshot))
        finish()
        redirectBack()

      case xs => {
        S.error(xs)
        if (ajaxForms_?) {
          SetHtml(FormGUID, renderHtml())
        } else {
          Noop
        }
      }
    }
  }
}
