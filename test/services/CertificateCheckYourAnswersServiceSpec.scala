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
import pages.SaoNamePage
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import viewmodels.checkAnswers.{SaoEmailCommunicationChoiceSummary, SaoEmailSummary, SaoNameSummary}


class CertificateCheckYourAnswersServiceSpec extends SpecBase with GuiceOneAppPerSuite {

  "CertificateCheckYourAnswersService must generate the summaryList when all the userAnswers" - {

    def SUT        = app.injector.instanceOf[CertificateCheckYourAnswersService]
    given Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

    def userAnswers: UserAnswers = UserAnswers(
      userAnswersId,
      Json.obj(
        SaoNamePage.toString -> Json.obj(
          "Full name" -> "testName",
          "Email address" -> "test@testEmail.com",
          "Email communication choice" -> "testChoice"
        )
      )
    )

    "are present" in {
//      val nameUserAnswers = emptyUserAnswers.set(SaoNamePage, "testName").get
//      val emailUserAnswers = emptyUserAnswers.set(SaoNamePage, "test@testEmail.com").get
//      val emailCommunicationChoiceUserAnswers = emptyUserAnswers.set(SaoNamePage, "testChoice").get
//
      println(userAnswers)


//      val userAnswers = Seq(nameUserAnswers, emailUserAnswers,emailCommunicationChoiceUserAnswers)

//      println(nameUserAnswers)
//
      val test = SUT.getSummaryList(userAnswers)
    }

    "are empty" in {
    }

  }

}
