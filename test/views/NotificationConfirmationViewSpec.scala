
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.html.NotificationConfirmationView
import views.NotificationConfirmationViewSpec.*

class NotificationConfirmationViewSpec extends ViewSpecBase[NotificationConfirmationView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "NotificationConfirmationView" - {
    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = true,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithOrWithoutError(hasError = false)
  }
}

object NotificationConfirmationViewSpec {
  val pageHeading = "notificationConfirmation"
  val pageTitle = "notificationConfirmation"
}
