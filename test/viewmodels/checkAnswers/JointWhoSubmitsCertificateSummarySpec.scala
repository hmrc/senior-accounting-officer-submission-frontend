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
import models.{CheckMode, JointWhoSubmitsCertificate}
import pages.JointWhoSubmitsCertificatePage

class JointWhoSubmitsCertificateSummarySpec extends CheckYourAnswersSummaryRenderingSupport {

  "JointWhoSubmitsCertificateSummary.row" - {

    "when there is no answer for JointWhoSubmitsCertificatePage" - {
      "must return None" in {
        JointWhoSubmitsCertificateSummary.row(emptyUserAnswers) mustBe None
      }
    }

    "when there is a user answer for JointWhoSubmitsCertificatePage" - {
      def testUserAnswers(answer: JointWhoSubmitsCertificate) =
        emptyUserAnswers.set(JointWhoSubmitsCertificatePage, answer).success.value

      "must render the expected key text" in {
        renderSummaryRow(
          JointWhoSubmitsCertificateSummary.row(testUserAnswers(JointWhoSubmitsCertificate.Sao)).value
        ).renderedKeyText mustBe
          "Who is submitting the certificate?"
      }

      "must render the SAO value" in {
        renderSummaryRow(
          JointWhoSubmitsCertificateSummary.row(testUserAnswers(JointWhoSubmitsCertificate.Sao)).value
        ).renderedValueText mustBe
          "I am the Senior Accounting Officer"
      }

      "must render the proxy value" in {
        renderSummaryRow(
          JointWhoSubmitsCertificateSummary.row(testUserAnswers(JointWhoSubmitsCertificate.Proxy)).value
        ).renderedValueText mustBe
          "I am authorised to submit the certificate on behalf of the Senior Accounting Officer"
      }

      "must render the expected action link" in {
        val action = renderSummaryRow(
          JointWhoSubmitsCertificateSummary.row(testUserAnswers(JointWhoSubmitsCertificate.Sao)).value
        ).renderedActionLink

        action.attr("href") mustBe routes.JointWhoSubmitsCertificateController.onPageLoad(CheckMode).url
        action.select("span.govuk-visually-hidden").text() mustBe "JointWhoSubmitsCertificate"
        action.select("span.govuk-visually-hidden").remove()
        action.text() mustBe "Change"
      }
    }
  }
}
