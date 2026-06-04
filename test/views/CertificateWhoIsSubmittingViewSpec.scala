/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views

import base.ViewSpecBase
import forms.CertificateWhoIsSubmittingFormProvider
import models.CertificateWhoIsSubmitting
import models.Mode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.CertificateWhoIsSubmittingViewSpec.*
import views.html.CertificateWhoIsSubmittingView

class CertificateWhoIsSubmittingViewSpec extends ViewSpecBase[CertificateWhoIsSubmittingView] {

  private val formProvider                           = app.injector.instanceOf[CertificateWhoIsSubmittingFormProvider]
  private val form: Form[CertificateWhoIsSubmitting] = formProvider()

  private def generateView(form: Form[CertificateWhoIsSubmitting], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "CertificateWhoIsSubmittingView" - {

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

          doc.createTestsWithCaption(pageCaption)
          doc.createTestsWithParagraphs(paragraphs)

          doc.createTestsWithRadioButtons(
            name = "value",
            radios = List(
              radio(value = option1key, label = option1Label),
              radio(value = option2key, label = option2Label)
            ),
            isChecked = None,
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateWhoIsSubmittingController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("value" -> option1key)), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          doc.createTestsWithCaption(pageCaption)
          doc.createTestsWithParagraphs(paragraphs)

          doc.createTestsWithRadioButtons(
            name = "value",
            radios = List(
              radio(value = option1key, label = option1Label),
              radio(value = option2key, label = option2Label)
            ),
            isChecked = Some(radio(value = option1key, label = option1Label)),
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateWhoIsSubmittingController.onSubmit(mode),
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

          doc.createTestsWithCaption(pageCaption)
          doc.createTestsWithParagraphs(paragraphs)

          doc.createTestsWithRadioButtons(
            name = "value",
            radios = List(
              radio(value = option1key, label = option1Label),
              radio(value = option2key, label = option2Label)
            ),
            isChecked = None,
            hasError = true
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateWhoIsSubmittingController.onSubmit(mode),
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

object CertificateWhoIsSubmittingViewSpec {
  val pageCaption             = "Submit a certificate"
  val pageHeading             = "Who is submitting the certificate?"
  val pageTitle               = "Who is submitting the certificate?"
  val paragraphs: Seq[String] = Seq(
    "HMRC need to know if the certificate will be submitted by the SAO or by someone authorised to act on their behalf. This allows us to show the correct declaration for the person submitting it.",
    "We rely on the information provided to be accurate. The SAO is responsible for reviewing and approving the information before it is submitted, and for authorising someone to submit it on their behalf if they are not submitting it themselves."
  )
  val option1key   = "sao"
  val option1Label = "I am the SAO"
  val option2key   = "standIn"
  val option2Label = "I am authorised to submit on behalf of the SAO"
}
