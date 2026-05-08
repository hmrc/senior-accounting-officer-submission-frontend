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
import forms.NotificationPreviousSaoNameFormProvider
import models.Mode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.NotificationPreviousSaoNameViewSpec.*
import views.html.NotificationPreviousSaoNameView

class NotificationPreviousSaoNameViewSpec extends ViewSpecBase[NotificationPreviousSaoNameView] {

  private val formProvider       = app.injector.instanceOf[NotificationPreviousSaoNameFormProvider]
  private val form: Form[String] = formProvider()

  private def generateView(form: Form[String], mode: Mode, index: Int): Document = {
    val view = SUT(form, mode, index)
    Jsoup.parse(view.toString)
  }

  val testIndex = 100

  "NotificationPreviousSaoNameView" - {

    Mode.values.foreach { mode =>
      s"when using $mode" - {
        "when the form is not filled in" - {
          val doc = generateView(form, mode, testIndex)

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

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.NotificationPreviousSaoNameController.onSubmit(mode, testIndex),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("value" -> testInputValue)), mode, testIndex)

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
            action = controllers.routes.NotificationPreviousSaoNameController.onSubmit(mode, testIndex),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form has errors" - {
          val doc = generateView(form.withError("value", "broken"), mode, testIndex)

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
            action = controllers.routes.NotificationPreviousSaoNameController.onSubmit(mode, testIndex),
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

object NotificationPreviousSaoNameViewSpec {
  val pageHeading    = "notificationPreviousSaoName"
  val pageTitle      = "notificationPreviousSaoName"
  val testInputValue = "myTestInputValue"
}
