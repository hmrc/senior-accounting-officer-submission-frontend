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
import forms.IsThisTheSaoOnCertificateFormProvider
import models.Mode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.IsThisTheSaoOnCertificateViewSpec.*
import views.html.IsThisTheSaoOnCertificateView

class IsThisTheSaoOnCertificateViewSpec extends ViewSpecBase[IsThisTheSaoOnCertificateView] {

  private val formProvider        = app.injector.instanceOf[IsThisTheSaoOnCertificateFormProvider]
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

          doc.createTestsWithCaption(pageCaption)

          doc.createTestsWithRadioButtons(
            name = "value",
            radios = List(
              radio(value = yesKey, label = yesLabel),
              radio(value = noKey, label = noLabel)
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
              radio(value = noKey, label = noLabel)
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
              radio(value = noKey, label = noLabel)
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
  val pageHeading = "Is Jackson Brown named as the Senior Accounting Officer (SAO) on this certificate?"
  val pageTitle   = "Submit a certificate"
  val pageCaption = "Submit a certificate"
  val yesKey      = "true"
  val yesLabel    = "Yes"
  val noKey       = "false"
  val noLabel     = "No"
}
