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
import navigation.Navigator
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar.*
import pages.NotificationGuidancePage
import play.api.http.HeaderNames
import play.api.inject
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.NotificationGuidanceView

class NotificationGuidanceControllerSpec extends SpecBase {

  "NotificationGuidance Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.NotificationGuidanceController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NotificationGuidanceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(using request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a POST" in {

      val mockNavigator = mock[Navigator]

      val testCall = Call("", "/testUrl")

      when(mockNavigator.nextPage(any(), any(), any()))
        .thenReturn(testCall)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .bindings(inject.bind[Navigator].toInstance(mockNavigator))
        .build()
      running(application) {
        val request = FakeRequest(POST, routes.NotificationGuidanceController.onSubmit().url)

        val result = route(application, request).value

        verify(mockNavigator).nextPage(meq(NotificationGuidancePage), any(), any())

        status(result) mustEqual SEE_OTHER
        header(HeaderNames.LOCATION, result) mustEqual Some(testCall.url)
      }
    }
  }
}
