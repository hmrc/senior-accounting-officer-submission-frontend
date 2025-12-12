
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.html.CertificateCheckYourAnswersView
import views.CertificateCheckYourAnswersViewSpec.*

class CertificateCheckYourAnswersViewSpec extends ViewSpecBase[CertificateCheckYourAnswersView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "CertificateCheckYourAnswersView" - {
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

object CertificateCheckYourAnswersViewSpec {
  val pageHeading = "certificateCheckYourAnswers"
  val pageTitle = "certificateCheckYourAnswers"
}
