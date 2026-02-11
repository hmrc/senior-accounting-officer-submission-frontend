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

import base.SpecBase
import controllers.routes
import models.CheckMode
import models.UserAnswers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.NotificationAdditionalInformationPage
import play.api.i18n.{Messages, MessagesApi}
import uk.gov.hmrc.govukfrontend.views.Implicits.RichString

class NotificationAdditionalInformationSummarySpec extends SpecBase with GuiceOneAppPerSuite {
  given Messages                   = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)
  val testUserAnswers: UserAnswers = emptyUserAnswers
  val testDate: String             = testUserAnswers.getFinancialYearEndDate

  "NotificationAdditionalInformationSummary.row" - {
    "must have expected key" in {
      val SUT = NotificationAdditionalInformationSummary.row(testUserAnswers)
      SUT.key mustBe "Additional information".toKey
    }

    "expected value" - {
      "must show answer when user answers contains additional information" in {
        val testAdditionalInformationAnswer = "apple"
        val testUserAnswersWithValue        =
          testUserAnswers.set(NotificationAdditionalInformationPage, Some(testAdditionalInformationAnswer)).get
        val SUT = NotificationAdditionalInformationSummary.row(testUserAnswersWithValue)
        SUT.value.content mustBe testAdditionalInformationAnswer.toText
      }

      "must be blank when user answers does not contain additional information" in {
        val SUT = NotificationAdditionalInformationSummary.row(testUserAnswers)
        SUT.value.content mustBe "".toText
      }
    }

    "expected action" - {
      def actions = NotificationAdditionalInformationSummary.row(testUserAnswers).actions

      "must only have one action" in {
        withClue("must be 1 action\n") {
          actions.size mustBe 1
        }
        withClue("must be 1 item in the action\n") {
          actions.head.items.size mustBe 1
        }
      }

      def action = actions.head.items.head

      "must have expected text" in {
        action.content mustBe "Change".toText
      }

      "must have expected url" in {
        action.href mustBe routes.NotificationAdditionalInformationController
          .onPageLoad(CheckMode)
          .url
      }

      "must have expected hidden text" in {
        action.visuallyHiddenText.get mustBe s"the additional information supplied for the notification (Financial year end $testDate)"
      }
    }
  }
}
