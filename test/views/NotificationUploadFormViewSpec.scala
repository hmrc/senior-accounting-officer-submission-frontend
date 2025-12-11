
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.html.NotificationUploadFormView
import views.NotificationUploadFormViewSpec.*

class NotificationUploadFormViewSpec extends ViewSpecBase[NotificationUploadFormView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "NotificationUploadFormView" - {
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

object NotificationUploadFormViewSpec {
  val pageHeading = "notificationUploadForm"
  val pageTitle = "notificationUploadForm"
}
