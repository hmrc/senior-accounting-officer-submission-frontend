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

package viewmodels.checkAnswers.certificate

import controllers.certificate.routes as certificateRoutes
import models.{CheckMode, UserAnswers}
import org.jsoup.nodes.Element
import pages.certificate.CertificateAdditionalInformationPage
import viewmodels.checkAnswers.CheckYourAnswersSummaryRenderingSupport

class CertificateAdditionalInformationSummarySpec extends CheckYourAnswersSummaryRenderingSupport {
  val testUserAnswers: UserAnswers = emptyUserAnswers

  "CertificateAdditionalInformationSummary.row" - {
    "must render the expected key text" in {
      renderSummaryRow(CertificateAdditionalInformationSummary.row(testUserAnswers)).renderedKeyText mustBe
        "Additional information"
    }

    "must render the supplied value" in {
      val row = renderRow("apple")
      row.renderedValueText mustBe "apple"
    }

    "must render an empty value when no answer is present" in {
      renderSummaryRow(
        CertificateAdditionalInformationSummary.row(testUserAnswers)
      ).renderedValueText mustBe "Not provided"
    }

    "must render special characters without double escaping" in {
      val row = renderRow("O'Hara & Jones & Co")

      row.renderedValueText mustBe "O'Hara & Jones & Co"
      row.renderedValueHtml must not include "&amp;#x27;"
      row.renderedValueHtml must not include "&amp;amp;"
    }

    "must render the expected action link" in {
      val action = renderSummaryRow(CertificateAdditionalInformationSummary.row(testUserAnswers)).renderedActionLink

      action
        .attr("href") mustBe certificateRoutes.CertificateAdditionalInformationController.onPageLoad(CheckMode).url
      action.select("span.govuk-visually-hidden").text() mustBe
        s"the additional information supplied for the certificate"
      action.select("span.govuk-visually-hidden").remove()
      action.text() mustBe "Change"
    }

    def renderRow(answer: String): Element = {
      val answers = testUserAnswers.set(CertificateAdditionalInformationPage, Some(answer)).get
      renderSummaryRow(CertificateAdditionalInformationSummary.row(answers))
    }
  }

}
