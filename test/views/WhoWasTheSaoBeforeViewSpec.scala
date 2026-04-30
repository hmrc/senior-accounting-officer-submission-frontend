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
import models.{NormalMode, CheckMode, Mode}
import pages.WhoWasTheSaoBeforePage
import forms.WhoWasTheSaoBeforeFormProvider
import views.html.WhoWasTheSaoBeforeView
import views.WhoWasTheSaoBeforeViewSpec.*


class WhoWasTheSaoBeforeViewSpec extends ViewSpecBase[WhoWasTheSaoBeforeView] {

  private val formProvider = app.injector.instanceOf[WhoWasTheSaoBeforeFormProvider]
  private val form: Form[String] = formProvider()

  private def generateView(form: Form[String], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "WhoWasTheSaoBeforeView" - {

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
          doc.createTestsWithLargeCaption(pageCaption)

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.WhoWasTheSaoBeforeController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
          doc.createTestsForInputWidth()
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
            action = controllers.routes.WhoWasTheSaoBeforeController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )

          doc.createTestsForInputWidth()
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
            action = controllers.routes.WhoWasTheSaoBeforeController.onSubmit(mode),
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

  extension (doc: => Document) {
    def createTestsForInputWidth(): Unit = {
      "must have input with expected class 'govuk-input--width-20'" in {
        doc.getMainContent.select("input.govuk-input--width-20").size() mustBe 1
      }
    }
  }
}

object WhoWasTheSaoBeforeViewSpec {
  val pageHeading = "Who was the SAO before Jackson Brown?"
  val pageCaption = "Submit a notification"
  val pageHint = "This is the person who held the role before Jackson Brown"
  val pageTitle = "Senior Accounting Officer full name"
  val testInputValue = "test name"
}
