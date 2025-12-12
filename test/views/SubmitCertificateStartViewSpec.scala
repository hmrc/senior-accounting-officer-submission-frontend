
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.html.SubmitCertificateStartView
import views.SubmitCertificateStartViewSpec.*

class SubmitCertificateStartViewSpec extends ViewSpecBase[SubmitCertificateStartView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "SubmitCertificateStartView" - {
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

object SubmitCertificateStartViewSpec {
  val pageHeading = "submitCertificateStart"
  val pageTitle = "submitCertificateStart"
}
