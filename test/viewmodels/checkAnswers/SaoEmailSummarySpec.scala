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
import pages.SaoEmailPage

class SaoEmailSummarySpec extends CheckYourAnswersSummaryRenderingSupport {

  "SaoEmailSummary.row" - {

    "when there is no answer for SaoEmailPage" - {
      "must return None" in {
        def SUT = SaoEmailSummary.row(emptyUserAnswers)

        SUT mustBe None
      }
    }

    "when there is a user answer for SaoEmailPage" - {
      def testUserAnswers(answer: String) =
        emptyUserAnswers.set(SaoEmailPage, answer).get

      def renderedRow(answer: String): Element =
        renderSummaryRow(SaoEmailSummary.row(testUserAnswers(answer)).get)

      "must render the expected key text" in {
        renderedRow("user@test.com").renderedKeyText mustBe "Email address"
      }

      "must render the supplied value" in {
        renderedRow("testSaoEmail").renderedValueText mustBe "testSaoEmail"
      }

      "must render the expected action link" in {
        val action = renderedRow("user@test.com").renderedActionLink

        action.attr("href") mustBe routes.SaoEmailController.onPageLoad(CheckMode).url
        action.select("span.govuk-visually-hidden").text() mustBe "SaoEmail"
        action.select("span.govuk-visually-hidden").remove()
        action.text() mustBe "Change"
      }
    }
  }
}
