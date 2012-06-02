package code.snippet

import net.liftweb.http.js.JsCmds
import JsCmds._

object WizardAllLiftScreen extends DemoCssBoundLiftScreen {
  val firstName = field("First Name", "")
  val middleName = field("Middle Name", "")
  val lastName = field("Last Name", "")

  override def allTemplate = defaultAllTemplate
  def formName = "wizardAll"

  def finish() {
    AjaxOnDone.set(SetHtml("wizardAllResults", <b>All done!</b>))
  }
}