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

package viewmodels.checkAnswers

import controllers.routes
import models.CheckMode
import org.jsoup.nodes.Element
import pages.SaoEmailCommunicationChoicePage

class SaoEmailCommunicationChoiceSummarySpec extends CheckYourAnswersSummaryRenderingSupport {

  "SaoEmailCommunicationChoiceSummary.row" - {

    "when there is no answer for SaoEmailCommunicationChoicePage" - {
      "must return None" in {
        def SUT = SaoEmailCommunicationChoiceSummary.row(emptyUserAnswers)

        SUT mustBe None
      }
    }

    "when there is a user answer for SaoEmailCommunicationChoicePage" - {
      def testUserAnswers(answer: Boolean) =
        emptyUserAnswers.set(SaoEmailCommunicationChoicePage, answer).get

      def renderedRow(answer: Boolean): Element =
        renderSummaryRow(SaoEmailCommunicationChoiceSummary.row(testUserAnswers(answer)).get)

      "must render the expected key text" in {
        renderedRow(answer = true).renderedKeyText mustBe "Email communications"
      }

      "must render 'Yes' when the answer is true" in {
        renderedRow(answer = true).renderedValueText mustBe "Yes"
      }

      "must render 'No' when the answer is false" in {
        renderedRow(answer = false).renderedValueText mustBe "No"
      }

      "must render the expected action link" in {
        val action = renderedRow(answer = true).renderedActionLink

        action.attr("href") mustBe routes.SaoEmailCommunicationChoiceController.onPageLoad(CheckMode).url
        action.select("span.govuk-visually-hidden").text() mustBe "SaoEmailCommunicationChoice"
        action.select("span.govuk-visually-hidden").remove()
        action.text() mustBe "Change"
      }
    }
  }
}
