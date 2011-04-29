package code.snippet

import net.liftweb.http.js.JsCmds
import JsCmds._
import xml.NodeSeq

object CustomBindingLiftScreen extends DemoCssBoundLiftScreen {
  val firstName = field("First Name", "")
  bindToId(firstName, "firstName")

  val middleName = field("Middle Name", "")
  bindToId(middleName, "middleName")

  val lastName = field("Last Name", "")
  bindToId(lastName, "lastName", Self)

  def formName = "customBinding"

  def finish() {
    AjaxOnDone.set(SetHtml("customBindingResults", <b>All done!</b>))
  }

  override def defaultFieldNodeSeq:NodeSeq =
    <div>
      <label class="label field"></label>
      <span class="value fieldValue"></span>
      <span class="help"></span>
      <div class="errors">
        <div class="error"></div>
      </div>
    </div>
}