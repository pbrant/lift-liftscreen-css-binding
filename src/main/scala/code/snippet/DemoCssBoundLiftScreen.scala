package code.snippet

import gov.wicourts.jdash2.form.CssBoundLiftScreen

trait DemoCssBoundLiftScreen extends CssBoundLiftScreen {
  override def defaultToAjax_? : Boolean = true
}