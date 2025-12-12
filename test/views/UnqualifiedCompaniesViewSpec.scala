
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.html.UnqualifiedCompaniesView
import views.UnqualifiedCompaniesViewSpec.*

class UnqualifiedCompaniesViewSpec extends ViewSpecBase[UnqualifiedCompaniesView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "UnqualifiedCompaniesView" - {
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

object UnqualifiedCompaniesViewSpec {
  val pageHeading = "unqualifiedCompanies"
  val pageTitle = "unqualifiedCompanies"
}
