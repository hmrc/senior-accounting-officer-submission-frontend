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

package viewmodels.checkAnswers

import org.jsoup.nodes.Element
import pages.{NotificationAdditionalInformationPage, OneSaoSubmitNotificationFullNamePage}
import services.NotificationCheckYourAnswersService
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class NotificationCheckYourAnswersServiceSpec extends CheckYourAnswersSummaryRenderingSupport {
  def SUT: NotificationCheckYourAnswersService = app.injector.instanceOf[NotificationCheckYourAnswersService]
  "NotificationCheckYourAnswersService.list" - {
    "when there are no answers for OneSaoSubmitNotificationFullNamePage and NotificationAdditionalInformationPage" - {
      "must return 'Not provided'" in {
        val result = SUT.getSummaryList(emptyUserAnswers)

        result.rows.head.value.content mustBe HtmlContent(
          s"""<span data-test-id="additional-information-value">Not provided</span>"""
        )
      }
    }

    "when there are answers for OneSaoSubmitNotificationFullNamePage and NotificationAdditionalInformationPage" - {

      "when there are answers for OneSaoSubmitNotificationFullNamePage" - {

        def testUserAnswers(answer: String) = emptyUserAnswers.set(OneSaoSubmitNotificationFullNamePage, answer).get

        def renderedRow(answer: String): Element =
          renderNotificationSummaryRow(OneSaoSubmitNotificationFullNameSummary.row(testUserAnswers(answer)).get)

        "must render the expected key text" in {
          renderedRow("name").renderedKeyText mustBe "Senior Accounting Officer"
        }

        "must render the supplied value" in {
          renderedRow("testName").renderedValueText mustBe "testName"
        }

      }

      "when there are answers for NotificationAdditionalInformationPage" - {

        def testUserAnswers(answer: String) =
          emptyUserAnswers.set(NotificationAdditionalInformationPage, Option(answer)).get

        def renderedRow(answer: String): Element =
          renderNotificationSummaryRow(NotificationAdditionalInformationSummary.row(testUserAnswers(answer)))

        "must render the expected key text" in {
          renderedRow("additionalInformation").renderedKeyText mustBe "Additional information"
        }

        "must render the supplied value" in {
          renderedRow("additionalInformation").renderedValueText mustBe "additionalInformation"
        }

        "must render special characters without double escaping" in {
          val row = renderedRow("O'Hara & Jones & Co")

          row.renderedValueText mustBe "O'Hara & Jones & Co"
          row.renderedValueHtml must not include "&amp;#x27;"
          row.renderedValueHtml must not include "&amp;amp;"
        }
      }
    }
  }
}
