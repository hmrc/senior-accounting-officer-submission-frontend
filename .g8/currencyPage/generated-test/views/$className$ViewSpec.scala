
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.inject.Injector
import models.NormalMode
import pages.$className$Page
import forms.$className$FormProvider
import views.html.$className$View
import views.$className$ViewSpec.*
import models.Mode


class $className$ViewSpec extends ViewSpecBase[$className$View] {

  private val formProvider = app.injector.instanceOf[$className$FormProvider]
  private val form: Form[BigDecimal] = formProvider()

  private def generateView(form: Form[BigDecimal], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "$className$View" - {
    Mode.values.foreach { mode =>
      s"when using \$mode" - {
        "when the form is not filled in" - {
          val doc = generateView(form, mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          doc.createTestsWithASingleTextInput(
            name = "value",
            label = "$className$",
            value = "",
            hint = None,
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.$className$Controller.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("value" -> testInputValue)), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          doc.createTestsWithASingleTextInput(
            name = "value",
            label = "$className$",
            value = testInputValue,
            hint = None,
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.$className$Controller.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form has errors" - {
          val doc = generateView(form.withError("value", "broken"), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = true
          )

          doc.createTestsWithASingleTextInput(
            name = "value",
            label = pageHeading,
            value = "",
            hint = None,
            hasError = true
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.$className$Controller.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = true
          )
        }
      }
    }
  }
}

object $className$ViewSpec {
  val pageHeading = "$className$"
  val pageTitle = "$className$"
  val testInputValue = $minimum$.toString
}
