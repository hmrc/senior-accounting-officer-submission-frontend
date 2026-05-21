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
import models.{CheckMode, CombinedCertificateDeclarationSao}
import pages.CombinedCertificateDeclarationSaoPage

class CombinedCertificateDeclarationSaoSummarySpec extends CheckYourAnswersSummaryRenderingSupport {
  "CombinedCertificateDeclarationSaoSummary.row" - {
    "when there is no answer for CombinedCertificateDeclarationSaoPage" - {
      "must return None" in {
        CombinedCertificateDeclarationSaoSummary.row(emptyUserAnswers) mustBe None
      }
    }
    "when there is a user answer for CombinedCertificateDeclarationSaoPage" - {
      def testUserAnswers(answer: CombinedCertificateDeclarationSao) =
        emptyUserAnswers.set(CombinedCertificateDeclarationSaoPage, answer).success.value

      "must render the expected key text" in {
        renderSummaryRow(
          CombinedCertificateDeclarationSaoSummary
            .row(testUserAnswers(CombinedCertificateDeclarationSao("SAO", "Proxy")))
            .value
        ).renderedKeyText mustBe "CombinedCertificateDeclarationSao"
      }

      "must render both declaration values" in {
        renderSummaryRow(
          CombinedCertificateDeclarationSaoSummary
            .row(testUserAnswers(CombinedCertificateDeclarationSao("value 1", "value 2")))
            .value
        ).renderedValueText mustBe "value 1 value 2"
      }

      "must render special characters without double escaping" in {
        val row = renderSummaryRow(
          CombinedCertificateDeclarationSaoSummary
            .row(testUserAnswers(CombinedCertificateDeclarationSao("O'Hara", "Jones & Co")))
            .value
        )
        row.renderedValueText mustBe "O'Hara Jones & Co"
        row.renderedValueHtml must not include "&amp;#x27;"
        row.renderedValueHtml must not include "&amp;amp;"
      }

      "must render the expected action link" in {
        val action = renderSummaryRow(
          CombinedCertificateDeclarationSaoSummary
            .row(testUserAnswers(CombinedCertificateDeclarationSao("SAO", "Proxy")))
            .value
        ).renderedActionLink
        action.attr("href") mustBe routes.CombinedCertificateDeclarationSaoController.onPageLoad(CheckMode).url
        action.select("span.govuk-visually-hidden").text() mustBe "combinedCertificateDeclarationSao.change.hidden"
        action.select("span.govuk-visually-hidden").remove()
        action.text() mustBe "Change"
      }
    }
  }
}
