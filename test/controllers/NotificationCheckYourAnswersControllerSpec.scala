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

package controllers

import base.SpecBase
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.NotificationCheckYourAnswersView
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.NotificationAdditionalInformationSummary
import play.api.mvc.Request
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import services.NotificationCheckYourAnswersService

class NotificationCheckYourAnswersControllerSpec extends SpecBase {

  def onwardRoute: Call = Call("GET", "/foo")

  "NotificationCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockService = mock[NotificationCheckYourAnswersService]
      when(mockService.getSummaryList(any())(using any())).thenReturn(SummaryList())
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(bind[NotificationCheckYourAnswersService].toInstance(mockService)).build()

      running(application) {
        given request: Request[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.NotificationCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NotificationCheckYourAnswersView]

        given Messages = messages(application)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(SummaryList()).toString
        verify(mockService).getSummaryList(any()) (using any())
      }
    }

    "must redirect to the next page for a POST" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.NotificationCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.NotificationCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.NotificationCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
