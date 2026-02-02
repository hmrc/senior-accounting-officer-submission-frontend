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
import models.UserAnswers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.{IsThisTheSaoOnCertificatePage, SaoEmailCommunicationChoicePage, SaoEmailPage, SaoNamePage}
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import viewmodels.checkAnswers.{IsThisTheSaoOnCertificateSummary, SaoEmailCommunicationChoiceSummary, SaoEmailSummary, SaoNameSummary}


class CertificateCheckYourAnswersServiceSpec extends SpecBase with GuiceOneAppPerSuite {

  "CertificateCheckYourAnswersService must generate the summaryList when all the userAnswers" - {

    def SUT        = app.injector.instanceOf[CertificateCheckYourAnswersService]
    given Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

    "when 'No' is selected and Full Name is given" in {
      val userAnswers = emptyUserAnswers.set(IsThisTheSaoOnCertificatePage, false).get.set(SaoNamePage, "testName").get.set(SaoEmailPage, "test@testemail.com").get.set(SaoEmailCommunicationChoicePage, true).get

      SUT.getSummaryList(userAnswers) mustBe SummaryList(
        Seq(
          IsThisTheSaoOnCertificateSummary.row(userAnswers).get,
          SaoNameSummary.row(userAnswers).get,
          SaoEmailSummary.row(userAnswers).get,
          SaoEmailCommunicationChoiceSummary.row(userAnswers).get
        )
      )

      //Name wiped when No changed to Yes
      //Name no displayed when Yes selected

    }

    "are empty" in {
    }

  }

}
