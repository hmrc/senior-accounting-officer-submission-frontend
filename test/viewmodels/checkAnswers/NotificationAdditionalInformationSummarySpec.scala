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
import models.UserAnswers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import uk.gov.hmrc.govukfrontend.views.Implicits.RichString

class NotificationAdditionalInformationSummarySpec extends SpecBase with GuiceOneAppPerSuite {
  given Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)
  val testUserAnswers: UserAnswers = emptyUserAnswers
  val testDate: String = testUserAnswers.getFinancialYearEndDate
  "" - {
    "Hello Test" in {
      val SUT = NotificationAdditionalInformationSummary.row(testUserAnswers)
      SUT.key mustBe "Additional information".toKey
      SUT.actions.get.items.head.visuallyHiddenText.get mustBe s"the additional information supplied for the notification (Financial year end $testDate)"
    }
  }
}
