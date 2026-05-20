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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.inject.Injector
import play.api.data.Form
import forms.CertificateDeclarationStandInFormProvider
import models.CertificateDeclarationStandIn
import models.{NormalMode, CheckMode, Mode}
import pages.CertificateDeclarationStandInPage
import views.html.CertificateDeclarationStandInView
import views.CertificateDeclarationStandInViewSpec.*


class CertificateDeclarationStandInViewSpec extends ViewSpecBase[CertificateDeclarationStandInView] {

  private val formProvider = app.injector.instanceOf[CertificateDeclarationStandInFormProvider]
  private val form: Form[CertificateDeclarationStandIn] = formProvider()

  private def generateView(form: Form[CertificateDeclarationStandIn], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "CertificateDeclarationStandInView" - {

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
          doc.createTestMustShowTextInput(name = "StandInName", label = field1Label, value = "", hint = None, hasError = false)
          doc.createTestMustShowTextInput(name = "SaoName", label = field2Label, value = "", hint = None, hasError = false)

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateDeclarationStandInController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("StandInName" -> testInputValue1, "SaoName" -> testInputValue2)), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          doc.createTestMustShowNumberOfInputs(2)
          doc.createTestMustShowTextInput(name = "StandInName", label = field1Label, value = testInputValue1, hint = None, hasError = false)
          doc.createTestMustShowTextInput(name = "SaoName", label = field2Label, value = testInputValue2, hint = None, hasError = false)

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateDeclarationStandInController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form has errors" - {
          val doc = generateView(form.withError("StandInName", "broken").withError("SaoName", "broken"), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = true
          )

          doc.createTestMustShowNumberOfInputs(2)
          doc.createTestMustShowTextInput(name = "StandInName", label = field1Label, value = "", hint = None, hasError = true)
          doc.createTestMustShowTextInput(name = "SaoName", label = field2Label, value = "", hint = None, hasError = true)

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateDeclarationStandInController.onSubmit(mode),
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

object CertificateDeclarationStandInViewSpec {
  val pageHeading = "certificateDeclarationStandIn"
  val pageTitle = "certificateDeclarationStandIn"
  val field1Label = "StandInName"
  val field2Label = "SaoName"

  val testInputValue1 = "test value 1"
  val testInputValue2 = "test value 2"

}
