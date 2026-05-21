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
import models.{CheckMode, JointCertificateSubmissionDeclaration}
import pages.JointCertificateSubmissionDeclarationPage

class JointCertificateSubmissionDeclarationSummarySpec extends CheckYourAnswersSummaryRenderingSupport {
  "JointCertificateSubmissionDeclarationSummary.row" - {
    "when there is no answer for JointCertificateSubmissionDeclarationPage" - {
      "must return None" in {
        JointCertificateSubmissionDeclarationSummary.row(emptyUserAnswers) mustBe None
      }
    }
    "when there is a user answer for JointCertificateSubmissionDeclarationPage" - {
      def testUserAnswers(answer: JointCertificateSubmissionDeclaration) =
        emptyUserAnswers.set(JointCertificateSubmissionDeclarationPage, answer).success.value

      "must render the expected key text" in {
        renderSummaryRow(
          JointCertificateSubmissionDeclarationSummary
            .row(testUserAnswers(JointCertificateSubmissionDeclaration("SAO", "Proxy")))
            .value
        ).renderedKeyText mustBe "JointCertificateSubmissionDeclaration"
      }

      "must render both declaration values" in {
        renderSummaryRow(
          JointCertificateSubmissionDeclarationSummary
            .row(testUserAnswers(JointCertificateSubmissionDeclaration("value 1", "value 2")))
            .value
        ).renderedValueText mustBe "value 1 value 2"
      }

      "must render special characters without double escaping" in {
        val row = renderSummaryRow(
          JointCertificateSubmissionDeclarationSummary
            .row(testUserAnswers(JointCertificateSubmissionDeclaration("O'Hara", "Jones & Co")))
            .value
        )
        row.renderedValueText mustBe "O'Hara Jones & Co"
        row.renderedValueHtml must not include "&amp;#x27;"
        row.renderedValueHtml must not include "&amp;amp;"
      }

      "must render the expected action link" in {
        val action = renderSummaryRow(
          JointCertificateSubmissionDeclarationSummary
            .row(testUserAnswers(JointCertificateSubmissionDeclaration("SAO", "Proxy")))
            .value
        ).renderedActionLink
        action.attr("href") mustBe routes.JointCertificateSubmissionDeclarationController.onPageLoad(CheckMode).url
        action.select("span.govuk-visually-hidden").text() mustBe "jointCertificateSubmissionDeclaration.change.hidden"
        action.select("span.govuk-visually-hidden").remove()
        action.text() mustBe "Change"
      }
    }
  }
}
