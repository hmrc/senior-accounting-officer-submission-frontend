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
import models.{CertificateSubmissionDeclaration, CheckMode}
import pages.CertificateSubmissionDeclarationPage

class CertificateSubmissionDeclarationSummarySpec extends CheckYourAnswersSummaryRenderingSupport {

  "CertificateSubmissionDeclarationSummary.row" - {

    "when there is no answer for CertificateSubmissionDeclarationPage" - {
      "must return None" in {
        CertificateSubmissionDeclarationSummary.row(emptyUserAnswers) mustBe None
      }
    }

    "when there is a user answer for CertificateSubmissionDeclarationPage" - {
      def testUserAnswers(answer: CertificateSubmissionDeclaration) =
        emptyUserAnswers.set(CertificateSubmissionDeclarationPage, answer).get

      "must render the expected key text" in {
        renderSummaryRow(
          CertificateSubmissionDeclarationSummary
            .row(testUserAnswers(CertificateSubmissionDeclaration("SAO", "Proxy")))
            .get
        ).renderedKeyText mustBe "CertificateSubmissionDeclaration"
      }

      "must render both declaration values" in {
        renderSummaryRow(
          CertificateSubmissionDeclarationSummary
            .row(testUserAnswers(CertificateSubmissionDeclaration("value 1", "value 2")))
            .get
        ).renderedValueText mustBe "value 1 value 2"
      }

      "must render special characters without double escaping" in {
        val row = renderSummaryRow(
          CertificateSubmissionDeclarationSummary
            .row(testUserAnswers(CertificateSubmissionDeclaration("O'Hara", "Jones & Co")))
            .get
        )

        row.renderedValueText mustBe "O'Hara Jones & Co"
        row.renderedValueHtml must not include "&amp;#x27;"
        row.renderedValueHtml must not include "&amp;amp;"
      }

      "must render the expected action link" in {
        val action = renderSummaryRow(
          CertificateSubmissionDeclarationSummary
            .row(testUserAnswers(CertificateSubmissionDeclaration("SAO", "Proxy")))
            .get
        ).renderedActionLink

        action.attr("href") mustBe routes.CertificateSubmissionDeclarationController.onPageLoad(CheckMode).url
        action.select("span.govuk-visually-hidden").text() mustBe "certificateSubmissionDeclaration.change.hidden"
        action.select("span.govuk-visually-hidden").remove()
        action.text() mustBe "Change"
      }
    }
  }
}
