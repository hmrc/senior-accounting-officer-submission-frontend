
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.html.$className$View
import views.$className$ViewSpec.*

class $className$ViewSpec extends ViewSpecBase[$className$View] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "$className$View" - {
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

object $className$ViewSpec {
  val pageHeading = "$className;format="decap"$"
  val pageTitle = "$className;format="decap"$"
}
