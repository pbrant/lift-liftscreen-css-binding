package code.snippet

import net.liftweb.http._
import net.liftweb.http.js._
import JsCmds._
import net.liftweb.common._

object LocalActionLiftScreen extends DemoCssBoundLiftScreen {
  val name = field("Name", "", FieldBinding("name"))

  val nameCopy = field("Name (copy)", "", FieldBinding("nameCopy"))

  def formName = "localAction"

  override def additionalFormBindings = Full(bindLocalAction("#copyNameAction [onclick]", copy))

  private def copy(): JsCmd = {
    nameCopy.set(name.get)
    renderFormCmd
  }

  def finish() {
    AjaxOnDone.set(SetHtml("localActionResults", <b>All done!</b>))
  }
}