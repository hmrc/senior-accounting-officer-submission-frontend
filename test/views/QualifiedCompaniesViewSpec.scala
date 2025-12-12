
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.html.QualifiedCompaniesView
import views.QualifiedCompaniesViewSpec.*

class QualifiedCompaniesViewSpec extends ViewSpecBase[QualifiedCompaniesView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "QualifiedCompaniesView" - {
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

object QualifiedCompaniesViewSpec {
  val pageHeading = "qualifiedCompanies"
  val pageTitle = "qualifiedCompanies"
}
