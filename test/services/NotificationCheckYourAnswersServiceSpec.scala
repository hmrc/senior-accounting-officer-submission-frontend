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

import pages.{
  NotificationAdditionalInformationPage,
  NotificationMoreThanOneSaoPage,
  OneSaoSubmitNotificationFullNamePage
}
import services.NotificationCheckYourAnswersService
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class NotificationCheckYourAnswersServiceSpec extends CheckYourAnswersSummaryRenderingSupport {
  def SUT: NotificationCheckYourAnswersService = app.injector.instanceOf[NotificationCheckYourAnswersService]
  "NotificationCheckYourAnswersService.list" - {

    "OneSaoSubmitNotificationFullNamePage.row" - {

      "when MoreThanOneSao is No" - {
        "Full Name is answered, must show the Full Name row" in {
          val userAnswers = emptyUserAnswers
            .set(NotificationMoreThanOneSaoPage, false)
            .get
            .set(OneSaoSubmitNotificationFullNamePage, "testName")
            .get

          val result = SUT.getSummaryList(userAnswers)

          result.rows.head.key.content mustBe Text("Senior Accounting Officer")
          result.rows.head.value.content mustBe HtmlContent(s"""<span data-test-id="sao-name-value">testName</span>""")
        }

        "Full Name is empty, must not show the Full Name row" in {
          val userAnswers = emptyUserAnswers
            .set(NotificationMoreThanOneSaoPage, false)
            .get

          val result = SUT.getSummaryList(userAnswers)

          result.rows.find(row => row.key.content == Text("Senior Accounting Officer")) mustBe None
        }
      }

      "when MoreThanOneSao is Yes" - {
        "must not show the Full Name row even if Full Name is answered" in {
          val userAnswers = emptyUserAnswers
            .set(NotificationMoreThanOneSaoPage, true)
            .get
            .set(OneSaoSubmitNotificationFullNamePage, "testName")
            .get

          val result = SUT.getSummaryList(userAnswers)

          result.rows.find(row => row.key.content == Text("Senior Accounting Officer")) mustBe None

        }
      }
    }

    "NotificationAdditionalInformationPage row" - {

      "when MoreThanOneSao is Yes" - {

        "when there are no answers for NotificationAdditionalInformationPage, must return 'Not provided'" in {
          val userAnswers = emptyUserAnswers
            .set(NotificationMoreThanOneSaoPage, true)
            .get
          val result = SUT.getSummaryList(userAnswers)

          result.rows.head.value.content mustBe HtmlContent(
            s"""<span data-test-id="additional-information-value">Not provided</span>"""
          )
        }

        "when there are answers for NotificationAdditionalInformationPage, must return value" in {
          val userAnswers = emptyUserAnswers
            .set(NotificationMoreThanOneSaoPage, true)
            .get
            .set(NotificationAdditionalInformationPage, Some("testValue"))
            .get
          val result = SUT.getSummaryList(userAnswers)

          result.rows.head.value.content mustBe HtmlContent(
            s"""<span data-test-id="additional-information-value">testValue</span>"""
          )
        }
      }

      "when MoreThanOneSao is No" - {

        "when there are no answers for NotificationAdditionalInformationPage, must return 'Not provided'" in {
          val userAnswers = emptyUserAnswers
            .set(NotificationMoreThanOneSaoPage, false)
            .get
          val result = SUT.getSummaryList(userAnswers)

          result.rows.head.value.content mustBe HtmlContent(
            s"""<span data-test-id="additional-information-value">Not provided</span>"""
          )
        }

        "when there are answers for NotificationAdditionalInformationPage, must return value" in {
          val userAnswers = emptyUserAnswers
            .set(NotificationMoreThanOneSaoPage, false)
            .get
            .set(NotificationAdditionalInformationPage, Some("testValue"))
            .get
          val result = SUT.getSummaryList(userAnswers)

          result.rows.head.value.content mustBe HtmlContent(
            s"""<span data-test-id="additional-information-value">testValue</span>"""
          )
        }
      }

    }
  }
}
