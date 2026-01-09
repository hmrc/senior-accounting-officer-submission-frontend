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
import forms.NotificationAdditionalInformationFormProvider
import models.Mode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalactic.source.Position
import play.api.data.Form
import play.api.mvc.Call
import views.NotificationAdditionalInformationViewSpec.*
import views.html.NotificationAdditionalInformationView

class NotificationAdditionalInformationViewSpec extends ViewSpecBase[NotificationAdditionalInformationView] {

  private val formProvider               = app.injector.instanceOf[NotificationAdditionalInformationFormProvider]
  private val form: Form[Option[String]] = formProvider()

  private def generateView(form: Form[Option[String]], mode: Mode): Document = {
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

          doc.createTestMustShowNumberOfTextareas(1)
          doc.createTestMustShowTextarea(
            name = "value",
            label = textAreaLabel,
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

          doc.createTestsWithBulletPoints(
            bulletPoints
          )

          doc.createTestMustShowNumberOfTextareas(1)

          doc.createTestMustShowTextarea(
            name = "value",
            label = textAreaLabel,
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

          doc.createTestMustShowTextarea(
            name = "value",
            label = textAreaLabel,
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
        val buttons = target.select("button")

        buttons.size() mustBe buttonTexts.size
      }

      buttonTexts.zipWithIndex
        .foreach((buttonText, i) => {
          s"must have a submit button with text '$buttonText'" in {
            val button = target.select("button").get(i)
            withClue(
              s"Submit Button with text '$buttonText' not found\n"
            ) {
              button.text() mustBe buttonText
            }
          }
        })
    }
  }
}

object NotificationAdditionalInformationViewSpec {
  val pageHeading             = "Additional information"
  val pageTitle               = "Notification details"
  val testInputValue          = "myTestInputValue"
  val paragraphs: Seq[String] = Seq(
    "Tell us if there’s anything we should know about your notification or the companies listed.",
    "This could include:"
  )
  val bulletPoints: Seq[String] = Seq(
    "a company’s status changing, such as becoming dormant or going into liquidation",
    "anything else relevant to the companies listed"
  )
  val textAreaLabel = "Provide information about your notification"
}
