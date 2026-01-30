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
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.HeaderNames
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.CertificateCheckYourAnswersService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.CertificateCheckYourAnswersView

class CertificateCheckYourAnswersControllerSpec extends SpecBase {

  def onwardRoute: Call = Call("GET", "/foo")

  "CertificateCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockService = mock[CertificateCheckYourAnswersService]
      when(mockService.getSummaryList(any())(using any())).thenReturn(SummaryList())

      val userAnswers = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[CertificateCheckYourAnswersService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.CertificateCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CertificateCheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(SummaryList())(using request, messages(application)).toString
        verify(mockService).getSummaryList(meq(userAnswers))(using any())
      }
    }

    "must redirect to the next page for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.CertificateCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        header(HeaderNames.LOCATION, result) mustEqual Some(onwardRoute.url)
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.CertificateCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.CertificateCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
