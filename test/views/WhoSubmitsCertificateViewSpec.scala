/*
 * Copyright 2025 HM Revenue & Customs
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
import forms.WhoSubmitsCertificateFormProvider
import models.Mode
import models.WhoSubmitsCertificate
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.WhoSubmitsCertificateViewSpec.*
import views.html.WhoSubmitsCertificateView

class WhoSubmitsCertificateViewSpec extends ViewSpecBase[WhoSubmitsCertificateView] {

  private val formProvider                      = app.injector.instanceOf[WhoSubmitsCertificateFormProvider]
  private val form: Form[WhoSubmitsCertificate] = formProvider()

  private def generateView(form: Form[WhoSubmitsCertificate], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "WhoSubmitsCertificateView" - {

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
            action = controllers.routes.WhoSubmitsCertificateController.onSubmit(mode),
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
            action = controllers.routes.WhoSubmitsCertificateController.onSubmit(mode),
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
            action = controllers.routes.WhoSubmitsCertificateController.onSubmit(mode),
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

object WhoSubmitsCertificateViewSpec {
  val pageHeading             = "Who is submitting the certificate?"
  val pageTitle               = "Submit a certificate"
  val pageCaption             = "Submit a certificate"
  val paragraphs: Seq[String] = Seq(
    "We need to know if your certificate will be submitted by the Senior Accounting Officer (SAO) or by someone authorised to act on their behalf. This helps us show the correct declaration for you to confirm and sign."
  )
  val option1key   = "sao"
  val option1Label = "I am the Senior Accounting Officer"
  val option2key   = "proxy"
  val option2Label = "I am authorised to submit the certificate on behalf of the Senior Accounting Officer"
}
