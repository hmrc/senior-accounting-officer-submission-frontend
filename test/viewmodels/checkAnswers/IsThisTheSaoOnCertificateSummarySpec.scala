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
import pages.IsThisTheSaoOnCertificatePage

class IsThisTheSaoOnCertificateSummarySpec extends CheckYourAnswersSummaryRenderingSupport {

  "IsThisTheSaoOnCertificateSummary.row" - {

    "when there is no answer for IsThisTheSaoOnCertificatePage" - {
      "must return None" in {
        IsThisTheSaoOnCertificateSummary.row(emptyUserAnswers) mustBe None
      }
    }

    "when there is a user answer for IsThisTheSaoOnCertificatePage" - {
      def testUserAnswers(answer: Boolean) =
        emptyUserAnswers.set(IsThisTheSaoOnCertificatePage, answer).success.value

      "must render the expected key text" in {
        renderSummaryRow(
          IsThisTheSaoOnCertificateSummary.row(testUserAnswers(answer = true)).value
        ).renderedKeyText mustBe
          "Is Jackson Brown named as the Senior Accounting Officer (SAO) on this certificate?"
      }

      "must render 'Yes' when the answer is true" in {
        renderSummaryRow(
          IsThisTheSaoOnCertificateSummary.row(testUserAnswers(answer = true)).value
        ).renderedValueText mustBe
          "Yes"
      }

      "must render 'No' when the answer is false" in {
        renderSummaryRow(
          IsThisTheSaoOnCertificateSummary.row(testUserAnswers(answer = false)).value
        ).renderedValueText mustBe
          "No"
      }

      "must render the expected action link" in {
        val action =
          renderSummaryRow(
            IsThisTheSaoOnCertificateSummary.row(testUserAnswers(answer = true)).value
          ).renderedActionLink

        action.attr("href") mustBe routes.IsThisTheSaoOnCertificateController.onPageLoad(CheckMode).url
        action.select("span.govuk-visually-hidden").text() mustBe "IsThisTheSaoOnCertificate"
        action.select("span.govuk-visually-hidden").remove()
        action.text() mustBe "Change"
      }
    }
  }
}
