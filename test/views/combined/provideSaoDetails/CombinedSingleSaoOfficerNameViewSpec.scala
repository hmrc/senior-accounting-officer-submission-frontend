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
import forms.notification.NotificationSingleSaoOfficerNameFormProvider
import models.Mode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.combined.provideSaoDetails.CombinedSingleSaoOfficerNameViewSpec.*
import views.html.combined.provideSaoDetails.CombinedSingleSaoOfficerNameView

class CombinedSingleSaoOfficerNameViewSpec extends ViewSpecBase[CombinedSingleSaoOfficerNameView] {

  private val formProvider       = app.injector.instanceOf[NotificationSingleSaoOfficerNameFormProvider]
  private val form: Form[String] = formProvider()

  private def generateView(form: Form[String], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "NotificationSingleSaoOfficerNameView" - {

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
            hint = None,
            hasError = false
          )

          doc.createTestsForInputWidth()

          doc.createTestsWithCaption(pageCaption)

          doc.createTestsWithSubmissionButton(
            action = provideSaoDetailsRoutes.CombinedSingleSaoOfficerNameController.onSubmit(mode),
            buttonText = "Continue"
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
            hint = None,
            hasError = false
          )

          doc.createTestsForInputWidth()

          doc.createTestsWithCaption(pageCaption)

          doc.createTestsWithSubmissionButton(
            action = provideSaoDetailsRoutes.CombinedSingleSaoOfficerNameController.onSubmit(mode),
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

          doc.createTestsWithASingleTextInput(
            name = "value",
            label = pageHeading,
            value = "",
            hint = None,
            hasError = true
          )

          doc.createTestsForInputWidth()

          doc.createTestsWithCaption(pageCaption)

          doc.createTestsWithSubmissionButton(
            action = provideSaoDetailsRoutes.CombinedSingleSaoOfficerNameController.onSubmit(mode),
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

object CombinedSingleSaoOfficerNameViewSpec {
  val pageHeading = "What is the name of the SAO?"
  val pageTitle   = "What is the name of the SAO? - Submit a notification and certificate"
  val pageCaption = "Provide the SAO’s details"

  val testInputValue = "Firstname Lastname"
}
