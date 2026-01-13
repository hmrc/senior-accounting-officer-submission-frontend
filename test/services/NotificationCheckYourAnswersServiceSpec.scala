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

package services

import base.SpecBase
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.NotificationAdditionalInformationPage
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.NotificationAdditionalInformationSummary

class NotificationCheckYourAnswersServiceSpec extends SpecBase with GuiceOneAppPerSuite {
  "NotificationCheckYourAnswersService must generate the summaryList when all the userAnswers" - {
    def SUT        = app.injector.instanceOf[NotificationCheckYourAnswersService]
    given Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

    "are present" in {
      val userAnswers = emptyUserAnswers.set(NotificationAdditionalInformationPage, Some("someValue")).get
      SUT.getSummaryList(userAnswers) mustBe SummaryList(
        Seq(
          NotificationAdditionalInformationSummary.row(userAnswers)
        )
      )
    }

    "are empty" in {
      SUT.getSummaryList(emptyUserAnswers) mustBe SummaryList(
        Seq(
          NotificationAdditionalInformationSummary.row(emptyUserAnswers)
        )
      )
    }
  }
}
