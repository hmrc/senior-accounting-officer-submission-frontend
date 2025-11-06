
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.inject.Injector
import play.api.data.Form
import forms.$className$FormProvider
import models.$className$
import models.{NormalMode, CheckMode, Mode}
import pages.$className$Page
import views.html.$className$View
import views.$className$ViewSpec.*


class $className$ViewSpec extends ViewSpecBase[$className$View] {

  private val formProvider = app.injector.instanceOf[$className$FormProvider]
  private val form: Form[$className$] = formProvider()

  private def generateView(form: Form[$className$], mode: Mode): Document = {
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

          doc.createTestMustShowNumberOfInputs(2)
          doc.createTestMustShowTextInput(name = "$field1Name$", label = field1Label, value = "", hint = None, hasError = false)
          doc.createTestMustShowTextInput(name = "$field2Name$", label = field2Label, value = "", hint = None, hasError = false)

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.$className$Controller.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("$field1Name$" -> testInputValue1, "$field2Name$" -> testInputValue2)), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          doc.createTestMustShowNumberOfInputs(2)
          doc.createTestMustShowTextInput(name = "$field1Name$", label = field1Label, value = testInputValue1, hint = None, hasError = false)
          doc.createTestMustShowTextInput(name = "$field2Name$", label = field2Label, value = testInputValue2, hint = None, hasError = false)

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.$className$Controller.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form has errors" - {
          val doc = generateView(form.withError("FieldA", "broken").withError("FieldB", "broken"), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = true
          )

          doc.createTestMustShowNumberOfInputs(2)
          doc.createTestMustShowTextInput(name = "$field1Name$", label = field1Label, value = "", hint = None, hasError = true)
          doc.createTestMustShowTextInput(name = "$field2Name$", label = field2Label, value = "", hint = None, hasError = true)

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
  val pageHeading = "$className;format="decap"$"
  val pageTitle = "$className;format="decap"$"
  val field1Label = "$field1Name$"
  val field2Label = "$field2Name$"

  val testInputValue1 = "test value 1"
  val testInputValue2 = "test value 2"

}
