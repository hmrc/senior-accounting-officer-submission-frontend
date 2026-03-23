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
import models.{CheckMode, WhoSubmitsCertificate}
import pages.WhoSubmitsCertificatePage

class WhoSubmitsCertificateSummarySpec extends CheckYourAnswersSummaryRenderingSupport {

  "WhoSubmitsCertificateSummary.row" - {

    "when there is no answer for WhoSubmitsCertificatePage" - {
      "must return None" in {
        WhoSubmitsCertificateSummary.row(emptyUserAnswers) mustBe None
      }
    }

    "when there is a user answer for WhoSubmitsCertificatePage" - {
      def testUserAnswers(answer: WhoSubmitsCertificate) =
        emptyUserAnswers.set(WhoSubmitsCertificatePage, answer).get

      "must render the expected key text" in {
        renderSummaryRow(
          WhoSubmitsCertificateSummary.row(testUserAnswers(WhoSubmitsCertificate.Sao)).get
        ).renderedKeyText mustBe
          "Who is submitting the certificate?"
      }

      "must render the SAO value" in {
        renderSummaryRow(
          WhoSubmitsCertificateSummary.row(testUserAnswers(WhoSubmitsCertificate.Sao)).get
        ).renderedValueText mustBe
          "I am the Senior Accounting Officer"
      }

      "must render the proxy value" in {
        renderSummaryRow(
          WhoSubmitsCertificateSummary.row(testUserAnswers(WhoSubmitsCertificate.Proxy)).get
        ).renderedValueText mustBe
          "I am authorised to submit the certificate on behalf of the Senior Accounting Officer"
      }

      "must render the expected action link" in {
        val action = renderSummaryRow(
          WhoSubmitsCertificateSummary.row(testUserAnswers(WhoSubmitsCertificate.Sao)).get
        ).renderedActionLink

        action.attr("href") mustBe routes.WhoSubmitsCertificateController.onPageLoad(CheckMode).url
        action.select("span.govuk-visually-hidden").text() mustBe "WhoSubmitsCertificate"
        action.select("span.govuk-visually-hidden").remove()
        action.text() mustBe "Change"
      }
    }
  }
}
