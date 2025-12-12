
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.html.SubmitNotificationView
import views.SubmitNotificationViewSpec.*

class SubmitNotificationViewSpec extends ViewSpecBase[SubmitNotificationView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "SubmitNotificationView" - {
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

object SubmitNotificationViewSpec {
  val pageHeading = "submitNotification"
  val pageTitle = "submitNotification"
}
