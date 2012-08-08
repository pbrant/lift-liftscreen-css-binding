package code.snippet

import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.util._
import Helpers._

object ReviewScreen {
  object restoreCheckout extends RequestVar[() => Unit](() => ())

  def render = {
    ".item" #> CheckoutScreen.screenFields.map ( field =>
        ".name" #> field.name & ".value" #> field.get.toString
    ) & "#returnButton" #> SHtml.ajaxButton("Return to Checkout", () => S.seeOther("checkout", restoreCheckout.get))
  }
}
