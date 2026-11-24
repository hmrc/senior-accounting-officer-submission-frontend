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

package views.combined.provideSaoDetails

import base.ViewSpecBase
import controllers.combined.provideSaoDetails.routes as provideSaoDetailsRoutes
import forms.notification.NotificationMultiSaoPreviousOfficerNameFormProvider
import models.Mode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.combined.provideSaoDetails.CombinedMultiSaoPreviousOfficerNameViewSpec.*
import views.html.combined.provideSaoDetails.CombinedMultiSaoPreviousOfficerNameView

class CombinedMultiSaoPreviousOfficerNameViewSpec extends ViewSpecBase[CombinedMultiSaoPreviousOfficerNameView] {

  private val formProvider       = app.injector.instanceOf[NotificationMultiSaoPreviousOfficerNameFormProvider]
  private val form: Form[String] = formProvider()

  private def generateView(saoName: String, form: Form[String], mode: Mode): Document = {
    val view = SUT(saoName, form, mode, saoIndex)
    Jsoup.parse(view.toString)
  }

  "CombinedMultiSaoPreviousOfficerNameView" - {

    Mode.values.foreach { mode =>
      s"when using $mode" - {
        "when the form is not filled in" - {
          val doc = generateView(saoName, form, mode)

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
            hint = None,
            hasError = false
          )
          doc.createTestsWithCaption(pageCaption)

          doc.createTestsWithSubmissionButton(
            action = provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerNameController.onSubmit(mode, saoIndex),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
          doc.createTestsForInputWidth()
        }

        "when the form is filled in" - {
          val doc = generateView(saoName, form.bind(Map("value" -> testInputValue)), mode)

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
            hint = None,
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerNameController.onSubmit(mode, saoIndex),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )

          doc.createTestsForInputWidth()
        }

        "when the form has errors" - {
          val doc = generateView(saoName, form.withError("value", "broken"), mode)

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
            hint = None,
            hasError = true
          )

          doc.createTestsWithSubmissionButton(
            action = provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerNameController.onSubmit(mode, saoIndex),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = true
          )
          doc.createTestsForInputWidth()
        }
      }
    }
  }
}

object CombinedMultiSaoPreviousOfficerNameViewSpec {
  val pageHeading    = "Who was the SAO before Firstname Lastname?"
  val pageCaption    = "Provide the SAO’s details"
  val pageTitle      = "Who was the SAO before the last SAO? - Submit a notification and certificate"
  val testInputValue = "test name"
  val saoName        = "Firstname Lastname"
  val saoIndex       = 3
}
