
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.html.NotificationCheckYourAnswersView
import views.NotificationCheckYourAnswersViewSpec.*

class NotificationCheckYourAnswersViewSpec extends ViewSpecBase[NotificationCheckYourAnswersView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "NotificationCheckYourAnswersView" - {
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

object NotificationCheckYourAnswersViewSpec {
  val pageHeading = "notificationCheckYourAnswers"
  val pageTitle = "notificationCheckYourAnswers"
}
