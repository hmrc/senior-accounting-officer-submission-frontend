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
import forms.{NotificationAdditionalInformation, NotificationAdditionalInformationFormProvider}
import models.Mode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalactic.source.Position
import play.api.data.Form
import play.api.mvc.Call
import views.NotificationAdditionalInformationViewSpec.*
import views.html.NotificationAdditionalInformationView

class NotificationAdditionalInformationViewSpec extends ViewSpecBase[NotificationAdditionalInformationView] {

  private val formProvider = app.injector.instanceOf[NotificationAdditionalInformationFormProvider]
  private val form: Form[NotificationAdditionalInformation] = formProvider()

  private def generateView(form: Form[NotificationAdditionalInformation], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "NotificationAdditionalInformationView" - {

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

          doc.createTestsWithSubmissionButtons(
            action = controllers.routes.NotificationAdditionalInformationController.onSubmit(mode),
            buttonTexts = Seq("Continue", "Skip")
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

          doc.createTestsWithParagraphs(
            paragraphs
          )

          doc.createTestsWithASingleTextInput(
            name = "value",
            label = pageHeading,
            value = testInputValue,
            hint = None,
            hasError = false
          )

          doc.createTestsWithSubmissionButtons(
            action = controllers.routes.NotificationAdditionalInformationController.onSubmit(mode),
            buttonTexts = Seq("Continue", "Skip")
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

          doc.createTestsWithSubmissionButtons(
            action = controllers.routes.NotificationAdditionalInformationController.onSubmit(mode),
            buttonTexts = Seq("Continue", "Skip")
          )

          doc.createTestsWithOrWithoutError(
            hasError = true
          )
        }
      }
    }
  }

  extension (doc: Document) {
    def createTestsWithSubmissionButtons(
        action: Call,
        buttonTexts: Seq[String]
    )(using
        pos: Position
    ): Unit = {
      def target = doc.getMainContent
      s"must have a form which submits to '${action.method} ${action.url}'" in {
        val form = target.select("form")
        form.attr("method") mustBe action.method
        form.attr("action") mustBe action.url
        form.size() mustBe 1
      }

      s"must have ${buttonTexts.size} number of buttons" in {
        val button = target.select("input[type=submit]")

        button.size() mustBe buttonTexts.size
      }

      buttonTexts.zipWithIndex.foreach((buttonText, i) => {
        s"must have a submit button with text '$buttonText'" in {
          val button = target.select("input[type=submit]").get(i)
          withClue(
            s"Submit Button with text '$buttonText' not found\n"
          ) {
            button.attr("value") mustBe buttonText
          }
        }
      })
    }
  }

}

object NotificationAdditionalInformationViewSpec {
  val pageHeading    = "Additional information"
  val pageTitle      = "Notification details"
  val testInputValue = "myTestInputValue"
  val paragraphs      = Seq("Tell us if there’s anything we should know about your notification or the companies listed.", "This could include:")
}
