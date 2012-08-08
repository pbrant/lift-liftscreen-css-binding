package code.snippet

import net.liftweb.http._

object CheckoutScreen extends DemoCssBoundLiftScreen {
  val firstName = field("First Name", "", FieldBinding("firstName"))

  val lastName = field("Last Name", "", FieldBinding("lastName"))

  val email = field("Email", "", FieldBinding("email"))

  def formName = "checkout"

  override def finishButton = <button>Review</button>

  def finish() {
    val snapshot = createSnapshot
    def restore = snapshot.restore()
    def prepareReview = {
      restore
      ReviewScreen.restoreCheckout(restore _)
    }
    S.seeOther("review", prepareReview _)
  }

}
