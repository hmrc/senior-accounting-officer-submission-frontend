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

package views.notification

import base.ViewSpecBase
import controllers.notification.routes as notificationRoutes
import forms.notification.MoreSaoSubmitNotificationFullNameFormProvider
import models.Mode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.notification.MoreSaoSubmitNotificationFullNameView

import MoreSaoSubmitNotificationFullNameViewSpec.*

class MoreSaoSubmitNotificationFullNameViewSpec extends ViewSpecBase[MoreSaoSubmitNotificationFullNameView] {

  private val formProvider       = app.injector.instanceOf[MoreSaoSubmitNotificationFullNameFormProvider]
  private val form: Form[String] = formProvider()

  private def generateView(form: Form[String], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "MoreSaoSubmitNotificationFullNameView" - {

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
            label = pageLabel,
            value = "",
            hint = None,
            hasError = false
          )

          doc.createTestsWithParagraphs(paragraphs)

          doc.createTestsForInputWidth()

          doc.createTestsWithSubmissionButton(
            action = notificationRoutes.MoreSaoSubmitNotificationFullNameController.onSubmit(mode),
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
            label = pageLabel,
            value = testInputValue,
            hint = None,
            hasError = false
          )

          doc.createTestsWithParagraphs(paragraphs)

          doc.createTestsForInputWidth()

          doc.createTestsWithSubmissionButton(
            action = notificationRoutes.MoreSaoSubmitNotificationFullNameController.onSubmit(mode),
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
            label = pageLabel,
            value = "",
            hint = None,
            hasError = true
          )

          doc.createTestsWithParagraphs(paragraphs)

          doc.createTestsForInputWidth()

          doc.createTestsWithSubmissionButton(
            action = notificationRoutes.MoreSaoSubmitNotificationFullNameController.onSubmit(mode),
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

object MoreSaoSubmitNotificationFullNameViewSpec {
  val pageHeading              = "Senior Accounting Officer details"
  val pageTitle                = "Submit a notification - SAO full name"
  val pageCaption              = "Submit a notification"
  val paragraphs: List[String] = List(
    "You told us more than one SAO held the role during the financial year. Enter the name of the last person who held the role. We’ll then ask you for details of the others who held the role earlier in the year."
  )
  val pageLabel = "What is the name of the last SAO?"

  val testInputValue = "myTestInputValue"
}
