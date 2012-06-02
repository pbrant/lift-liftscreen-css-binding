package code.snippet

import xml.NodeSeq
import net.liftweb.http._

trait DemoCssBoundLiftScreen extends CssBoundLiftScreen {
  override def defaultToAjax_? : Boolean = true

  override def defaultFieldNodeSeq: NodeSeq =
    <div>
      <label class="label field"></label>
      <span class="value fieldValue"></span>
      <span class="help"></span>
      <div class="errors">
        <div class="error"></div>
      </div>
    </div>
}