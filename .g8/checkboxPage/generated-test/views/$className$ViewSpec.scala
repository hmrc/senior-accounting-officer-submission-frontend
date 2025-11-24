
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
  private val form: Form[Set[$className$]] = formProvider()

  private def generateView(form: Form[Set[$className$]], mode: Mode): Document = {
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

          "must display the correct checkbox labels" in {
            doc.getMainContent.select("label[for=value_0]").text() mustBe option1Label
            doc.getMainContent.select("label[for=value_1]").text() mustBe option2Label
          }

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.$className$Controller.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(hasError = false)
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("value[0]" -> option1Key, "value[1]" -> option2Key)), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          "must display the correct checkbox labels" in {
            doc.getMainContent.select("label[for=value_0]").text() mustBe option1Label
            doc.getMainContent.select("label[for=value_1]").text() mustBe option2Label
          }

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.$className$Controller.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(hasError = false)
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

          "must display the correct checkbox labels" in {
            doc.getMainContent.select("label[for=value_0]").text() mustBe option1Label
            doc.getMainContent.select("label[for=value_1]").text() mustBe option2Label
          }

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.$className$Controller.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(hasError = true)
        }
      }
    }
  }
}

object $className$ViewSpec {
  val pageHeading = "$title$"
  val pageTitle = "$title$"
  val option1Key = "$option1key;format="decap"$"
  val option1Label = "$option1msg$"
  val option2Key = "$option2key;format="decap"$"
  val option2Label = "$option2msg$"

}
