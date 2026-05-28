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
import forms.SubmissionTypeFormProvider
import models.Mode
import models.SubmissionType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.SubmissionTypeViewSpec.*
import views.html.SubmissionTypeView

class SubmissionTypeViewSpec extends ViewSpecBase[SubmissionTypeView] {

  private val formProvider               = app.injector.instanceOf[SubmissionTypeFormProvider]
  private val form: Form[SubmissionType] = formProvider()

  private def generateView(form: Form[SubmissionType], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "SubmissionTypeView" - {

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

          doc.createTestsWithRadioButtons(
            name = "value",
            radios = List(
              radio(value = option1key, label = option1Label),
              radio(value = option2key, label = option2Label),
              radio(value = option3key, label = option3Label)
            ),
            isChecked = None,
            hasError = false
          )

          doc.createTestsWithRadioButtonDivider(radioButtonDivider)

          doc.createTestsWithLargeCaption(pageCaption)

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.SubmissionTypeController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("value" -> option1key)), mode)

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
              radio(value = option1key, label = option1Label),
              radio(value = option2key, label = option2Label),
              radio(value = option3key, label = option3Label)
            ),
            isChecked = Some(radio(value = option1key, label = option1Label)),
            hasError = false
          )

          doc.createTestsWithRadioButtonDivider(radioButtonDivider)

          doc.createTestsWithLargeCaption(pageCaption)

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.SubmissionTypeController.onSubmit(mode),
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
              radio(value = option1key, label = option1Label),
              radio(value = option2key, label = option2Label),
              radio(value = option3key, label = option3Label)
            ),
            isChecked = None,
            hasError = true
          )

          doc.createTestsWithRadioButtonDivider(radioButtonDivider)

          doc.createTestsWithLargeCaption(pageCaption)

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.SubmissionTypeController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = true
          )
        }
      }
    }
  }

  extension (doc: Document) {
    def createTestsWithRadioButtonDivider(expectedText: String): Unit = {
      "must have a divider" in {
        doc.select(".govuk-radios__divider").size() mustBe 1
        doc.select(".govuk-radios__divider").text() mustBe expectedText
      }
    }
  }
}

object SubmissionTypeViewSpec {
  val pageHeading        = "What would you like to submit?"
  val pageTitle          = "Senior Accounting Officer notification and certificate - New submission"
  val pageCaption        = "New submission"
  val option1key         = "notification"
  val option1Label       = "A notification"
  val option2key         = "certificate"
  val option2Label       = "A certificate"
  val option3key         = "combined"
  val option3Label       = "A notification and certificate together"
  val radioButtonDivider = "or"
}
