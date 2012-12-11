package code.snippet

import net.liftweb.http.js.JsCmds
import JsCmds._

object WizardAllLiftScreen extends DemoCssBoundLiftScreen {
  val firstName = field("First Name", "")
  val middleName = field("Middle Name", "")
  val lastName = field("Last Name", "")

  def formName = "wizardAll"

  // Only necessary because DemoCssBoundLiftScreen overrides
  // allTemplate itself
  override def allTemplate = defaultAllTemplate

  def finish() {
    AjaxOnDone.set(SetHtml("wizardAllResults", <b>All done!</b>))
  }
}
