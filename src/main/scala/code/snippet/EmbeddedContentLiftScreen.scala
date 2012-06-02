package code.snippet

import net.liftweb.http.js.JsCmds
import JsCmds._

object EmbeddedContentLiftScreen extends DemoCssBoundLiftScreen {
  val firstName = field("First Name", "")
  val middleName = field("Middle Name", "")
  val lastName = field("Last Name", "")

  def formName = "embeddedContent"

  def finish() {
    AjaxOnDone.set(SetHtml("embeddedContentResults", <b>All done!</b>))
  }
}
