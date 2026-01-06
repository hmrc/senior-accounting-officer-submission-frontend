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
import models.{Mode, NotificationAdditionalInformation}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
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

          doc.createTestMustShowNumberOfInputs(1)
          doc.createTestMustShowTextArea(
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

          doc.createTestMustShowNumberOfInputs(1)

          doc.createTestMustShowTextArea(
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

          doc.createTestMustShowTextArea(
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

  extension (target: => Document | Element) {
    private def resolve: Element = target match {
      case doc: Document => doc.getMainContent
      case _             => target
    }

    private def safeSelect(cssQuery: String): Elements =
      if cssQuery.nonEmpty
      then target.resolve.select(cssQuery)
      else Elements()

    def createTestMustShowTextArea(
        name: String,
        label: String,
        value: String,
        hint: Option[String],
        hasError: Boolean
    )(using pos: Position): Unit = {

      s"for textarea '$name'" - {

        def textareaElements = target.resolve.select(s"textarea[name=$name].govuk-textarea")

        s"textarea with name '$name' must exist on the page" in {
          withClue(s"textarea with the name '$name' not found\n'") {
            textareaElements.size mustBe 1
          }
        }

        def textAreaElement = textareaElements.get(0)

        s"textarea with name '$name' must have a label '$label' with correct id and text" in {
          val textareaId = textAreaElement.attr("id")
          withClue(s"textarea with an 'id' attribute not found\n") {
            textareaId must not be ""
          }

          val labels = target.resolve.select(s"""label[for="$textareaId"]""")
          withClue(s"label for '$textareaId' not found\n") {
            labels.size() must be > 0
          }
          withClue(s"label text does not match expected label text '$label'\n") {
            labels.get(0).text mustEqual label
          }

          val erroredFormGroup = target.resolve.select(s""".govuk-error-summary a[href="#$textareaId"]""")
          if hasError then {
            withClue("error content must be shown\n") {
              erroredFormGroup.size mustBe 1
            }
          } else {
            withClue("error content must not be shown\n") {
              erroredFormGroup.size mustBe 0
            }
          }
        }

        s"textarea with name '$name' must have value of '$value'" in {
          withClue(s"textarea with name '$name' does not have a value attribute '$value'\n") {
            textAreaElement.text mustEqual value
          }
        }

        hint match {
          case Some(expectedHintText) => {
            def hintSelector = textAreaElement
              .attr("aria-describedby")
              .split(" ")
              .filter(_.nonEmpty)
              .map("#" + _ + ".govuk-hint")
              .mkString(",")

            def hints = target.resolve.safeSelect(hintSelector)

            s"textarea with name '$name' must have a hint with values '$expectedHintText'" in {
              withClue(s"for textarea with name '$name' hint element not found\n") {
                hints.size() must be > 0
              }
              withClue(s"textarea with name '$name' multiple hint elements found\n") {
                hints.size() mustBe 1
              }
              withClue(
                s"textarea with name '$name' hint text does not match expected value '$expectedHintText' not found\n"
              ) {
                hints.get(0).text mustEqual expectedHintText
              }
            }
          }
          case None =>
            s"textarea with name '$name' must not have an associated hint" in {
              val hints = target.resolve.select(".govuk-hint")

              withClue(s"for textarea with name '$name' hint element not found\n") {
                hints.size() mustBe 0
              }
            }
        }

        if hasError then {
          s"textarea with name '$name' must show an associated error message when field has error" in {

            val errorMessageElements = target.resolve.safeSelect(s".govuk-error-summary a[href=\"#$name\"]")

            withClue(s"textarea does not have expected error message with id '$name'\n") {
              errorMessageElements.size mustBe 1
            }
          }
        } else {
          s"textarea with name '$name' must not show an associated error message when field has no error" in {
            val errorMessageElements = target.resolve.select(".govuk-error-message")

            withClue(s"textarea has unexpected error message with id '$name'\n") {
              errorMessageElements.size mustBe 0
            }
          }
        }
      }
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
