package code.snippet

import net.liftweb.http.js.JsCmds
import JsCmds._
import net.liftweb.http._
import FieldBinding._

object CustomBindingLiftScreen extends DemoCssBoundLiftScreen {
  val firstName = field("First Name", "", FieldBinding("firstName"))

  val middleName = field("Middle Name", "", FieldBinding("middleName"))

  val lastName = field("Last Name", "", FieldBinding("lastName", Self))

  def formName = "customBinding"

  def finish() {
    AjaxOnDone.set(SetHtml("customBindingResults", <b>All done!</b>))
  }
}