package code.snippet

import net.liftweb.http.js.JsCmds
import JsCmds._

object LocalActionLiftScreen extends DemoCssBoundLiftScreen {
  val name = field("Name", "")
  bindToId(name, "name")

  val nameCopy = field("Name (copy)", "")
  bindToId(nameCopy, "nameCopy")

  def formName = "localAction"

  override def additionalFormBindings = Some(bindLocalAction("#copyNameAction", "copy"))

  override protected def localActions = {
    case "copy" => {
      nameCopy.set(name.get)
      renderFormCmd
    }
  }

  def finish() {
    Finished.set(true)
    AjaxOnDone.set(SetHtml("localActionResults", <b>All done!</b>))
  }
}