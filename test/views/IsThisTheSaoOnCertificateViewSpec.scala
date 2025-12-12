
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.inject.Injector
import play.api.data.Form
import forms.IsThisTheSaoOnCertificateFormProvider
import models.{NormalMode, CheckMode, Mode}
import pages.IsThisTheSaoOnCertificatePage
import views.html.IsThisTheSaoOnCertificateView
import views.IsThisTheSaoOnCertificateViewSpec.*


class IsThisTheSaoOnCertificateViewSpec extends ViewSpecBase[IsThisTheSaoOnCertificateView] {

  private val formProvider = app.injector.instanceOf[IsThisTheSaoOnCertificateFormProvider]
  private val form: Form[Boolean] = formProvider()

  private def generateView(form: Form[Boolean], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "IsThisTheSaoOnCertificateView" - {

    Mode.values.foreach { mode =>
      s"when using $mode" - {
        "when the form is not filled in" - {
          val doc = generateView(form, mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          doc.createTestsWithRadioButtons(
            name = "value",
            radios = List(
              radio(value = yesKey, label = yesLabel),
              radio(value = noKey, label = noLabel),
            ),
            isChecked = None,
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.IsThisTheSaoOnCertificateController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("value" -> yesKey)), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          doc.createTestsWithRadioButtons(
            name = "value",
            radios = List(
              radio(value = yesKey, label = yesLabel),
              radio(value = noKey, label = noLabel),
            ),
            isChecked = Some(radio(value = yesKey, label = yesLabel)),
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.IsThisTheSaoOnCertificateController.onSubmit(mode),
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

          doc.createTestsWithRadioButtons(
            name = "value",
            radios = List(
              radio(value = yesKey, label = yesLabel),
              radio(value = noKey, label = noLabel),
            ),
            isChecked = None,
            hasError = true
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.IsThisTheSaoOnCertificateController.onSubmit(mode),
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

object IsThisTheSaoOnCertificateViewSpec {
  val pageHeading = "isThisTheSaoOnCertificate"
  val pageTitle = "isThisTheSaoOnCertificate"
  val yesKey = "true"
  val yesLabel = "Yes"
  val noKey = "false"
  val noLabel = "No"
}
