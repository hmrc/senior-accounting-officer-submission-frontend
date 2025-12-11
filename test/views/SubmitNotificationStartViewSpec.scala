
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.html.SubmitNotificationStartView
import views.SubmitNotificationStartViewSpec.*

class SubmitNotificationStartViewSpec extends ViewSpecBase[SubmitNotificationStartView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "SubmitNotificationStartView" - {
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

object SubmitNotificationStartViewSpec {
  val pageHeading = "submitNotificationStart"
  val pageTitle = "submitNotificationStart"
}
