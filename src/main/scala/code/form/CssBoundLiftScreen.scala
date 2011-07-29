/*
 * Copyright 2011 WorldWide Conferencing, LLC
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
package code.form

import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmd
import net.liftweb.util.FieldError
import net.liftweb.http.S

trait CssBoundLiftScreen extends LocalLiftScreen with CssBoundScreen {
  protected val SavedDefaultXml = vendAVar(defaultXml)

  protected val Finished = vendATransientRequestVar(false)

  protected val LocalActionName = vendATransientRequestVar("")
  protected val NextId = vendATransientRequestVar("")

  override def localSetup() {
    SavedDefaultXml.get
  }

  override def allTemplate = SavedDefaultXml.get

  protected def defaultAllTemplate = super.allTemplate

  protected def additionalAjaxErrorResponse: JsCmd = Noop

  override protected def doFinish(): JsCmd= {
    if (! LocalAction.is.isEmpty && localActions.isDefinedAt(LocalAction.is))
      localActions(LocalAction.is)
    else {
      val result = super.doFinish
      if (Finished.is)
        result
      else
        result & additionalAjaxErrorResponse
    }
  }

  protected def renderWithErrors(errors: List[FieldError]) {
    S.error(errors)
    AjaxOnDone.set(renderFormCmd & additionalAjaxErrorResponse)
  }

  protected def renderFormCmd: JsCmd = SetHtml(FormGUID, renderHtml())
}
