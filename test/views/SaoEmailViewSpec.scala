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
import forms.SaoEmailFormProvider
import models.Mode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.SaoEmailViewSpec.*
import views.html.SaoEmailView

class SaoEmailViewSpec extends ViewSpecBase[SaoEmailView] {

  private val formProvider       = app.injector.instanceOf[SaoEmailFormProvider]
  private val form: Form[String] = formProvider()

  private def generateView(form: Form[String], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "SaoEmailView" - {

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
            action = controllers.routes.SaoEmailController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithCaption(
            pageCaption
          )

          doc.createTestMustShowHint(
            pageHint
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
            label = pageHeading,
            value = testInputValue,
            hint = Some(pageHint),
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.SaoEmailController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithCaption(
            pageCaption
          )

          doc.createTestMustShowHint(
            pageHint
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
            hint = Some(pageHint),
            hasError = true
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.SaoEmailController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithCaption(
            pageCaption
          )

          doc.createTestMustShowHint(
            pageHint
          )

          doc.createTestsWithOrWithoutError(
            hasError = true
          )
        }
      }
    }
  }
}

object SaoEmailViewSpec {
  val pageHeading    = "Enter {0}’s email address"
  val pageTitle      = "Senior Accounting Officer contact details"
  val pageCaption    = "Submit a certificate"
  val testInputValue = "myTestInputValue@test.com"
  val pageHint       = "We’ll only use this to contact them about the company’s submission."
}
