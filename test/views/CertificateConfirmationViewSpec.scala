
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.html.CertificateConfirmationView
import views.CertificateConfirmationViewSpec.*

class CertificateConfirmationViewSpec extends ViewSpecBase[CertificateConfirmationView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "CertificateConfirmationView" - {
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

object CertificateConfirmationViewSpec {
  val pageHeading = "certificateConfirmation"
  val pageTitle = "certificateConfirmation"
}
