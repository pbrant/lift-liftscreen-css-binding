package code.snippet

import net.liftweb.http.js.JsCmds
import JsCmds._

object CustomBindingLiftScreen extends DemoCssBoundLiftScreen {
  val firstName = field("First Name", "")
  bindToId(firstName, "firstName")

  val middleName = field("Middle Name", "")
  bindToId(middleName, "middleName")

  val lastName = field("Last Name", "")
  bindToId(lastName, "lastName", Self)

  def formName = "customBinding"

  def finish() {
    Finished.set(true)
    AjaxOnDone.set(SetHtml("customBindingResults", <b>All done!</b>))
  }
}