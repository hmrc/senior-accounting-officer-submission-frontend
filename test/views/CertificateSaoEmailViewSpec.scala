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
import forms.CertificateSaoEmailFormProvider
import models.Mode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.CertificateSaoEmailViewSpec.*
import views.html.CertificateSaoEmailView

class CertificateSaoEmailViewSpec extends ViewSpecBase[CertificateSaoEmailView] {

  private val formProvider       = app.injector.instanceOf[CertificateSaoEmailFormProvider]
  private val form: Form[String] = formProvider()

  private def generateView(form: Form[String], mode: Mode): Document = {
    val view = SUT("Firstname Lastname", form, mode)
    Jsoup.parse(view.toString)
  }

  "CertificateSaoEmailView" - {

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

          doc.createTestsWithASingleTextInput(
            name = "value",
            label = pageHeading,
            value = "",
            hint = Some(pageHint),
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateSaoEmailController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithLargeCaption(
            pageCaption
          )

          doc.createTestMustShowHint(
            pageHint
          )

          doc.createTestsForInputWidth()

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
            label = pageHeading,
            value = testInputValue,
            hint = Some(pageHint),
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateSaoEmailController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithLargeCaption(
            pageCaption
          )

          doc.createTestMustShowHint(
            pageHint
          )

          doc.createTestsForInputWidth()

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
            hint = Some(pageHint),
            hasError = true
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateSaoEmailController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsForInputWidth()

          doc.createTestsWithOrWithoutError(
            hasError = true
          )
        }
      }
    }
  }
}

object CertificateSaoEmailViewSpec {
  val pageHeading    = "What is the email address for Firstname Lastname?"
  val pageTitle      = "What is the email address for the SAO?"
  val testInputValue = "myTestInputValue"
  val pageCaption    = "Submit a certificate"
  val pageHint       = "We’ll only use this to contact them about the company’s submission"
}
