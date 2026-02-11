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
import forms.CertificateSubmissionDeclarationFormProvider
import models.CertificateSubmissionDeclaration
import models.Mode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.CertificateSubmissionDeclarationViewSpec.*
import views.html.CertificateSubmissionDeclarationView

class CertificateSubmissionDeclarationViewSpec extends ViewSpecBase[CertificateSubmissionDeclarationView] {

  private val formProvider = app.injector.instanceOf[CertificateSubmissionDeclarationFormProvider]
  private val form: Form[CertificateSubmissionDeclaration] = formProvider()

  private def generateView(form: Form[CertificateSubmissionDeclaration], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "CertificateSubmissionDeclarationView" - {

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

          doc.createTestMustShowNumberOfInputs(2)
          doc.createTestMustShowTextInput(name = "sao", label = field1Label, value = "", hint = None, hasError = false)
          doc.createTestMustShowTextInput(
            name = "proxy",
            label = field2Label,
            value = "",
            hint = None,
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateSubmissionDeclarationController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("sao" -> testInputValue1, "proxy" -> testInputValue2)), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          doc.createTestMustShowNumberOfInputs(2)
          doc.createTestMustShowTextInput(
            name = "sao",
            label = field1Label,
            value = testInputValue1,
            hint = None,
            hasError = false
          )
          doc.createTestMustShowTextInput(
            name = "proxy",
            label = field2Label,
            value = testInputValue2,
            hint = None,
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateSubmissionDeclarationController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form has errors" - {
          val doc = generateView(form.withError("sao", "broken").withError("proxy", "broken"), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = true
          )

          doc.createTestMustShowNumberOfInputs(2)
          doc.createTestMustShowTextInput(name = "sao", label = field1Label, value = "", hint = None, hasError = true)
          doc.createTestMustShowTextInput(name = "proxy", label = field2Label, value = "", hint = None, hasError = true)

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateSubmissionDeclarationController.onSubmit(mode),
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

object CertificateSubmissionDeclarationViewSpec {
  val pageHeading = "certificateSubmissionDeclaration"
  val pageTitle   = "certificateSubmissionDeclaration"
  val field1Label = "sao"
  val field2Label = "proxy"

  val testInputValue1 = "test value 1"
  val testInputValue2 = "test value 2"

}
