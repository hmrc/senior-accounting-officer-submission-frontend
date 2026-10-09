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

package controllers.notification

import base.SpecBase
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.NotificationSubmitService
import services.NotificationSubmitService.NotificationState
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.Future

class NotificationSubmittingControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val mockNotificationSubmitService: NotificationSubmitService = mock[NotificationSubmitService]

  override def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    super
      .applicationBuilder(Some(emptyUserAnswers))
      .overrides(
        inject.bind[NotificationSubmitService].toInstance(mockNotificationSubmitService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockNotificationSubmitService)
  }

  "NotificationPendingController.onPageLoad" - {
    "must return OK and the correct view" - {
      "when notificationSubmitService returns a pending result" in {
        when(mockNotificationSubmitService.getStateOfWorkItem(any())(using any()))
          .thenReturn(Future.successful(NotificationState.Pending("key")))

        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.NotificationSubmittingController.onPageLoad("key").url)
          val result  = route(application, request).value

          status(result) mustEqual Status.OK

          verify(mockNotificationSubmitService, times(1)).getStateOfWorkItem(any())(using any())
        }
      }
    }

    "must return a redirect to the notification confirmation page" - {
      "when notificationSubmitService returns a success result" in {
        when(mockNotificationSubmitService.getStateOfWorkItem(any())(using any()))
          .thenReturn(Future.successful(NotificationState.Success("notificationRef")))

        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.NotificationSubmittingController.onPageLoad("key").url)
          val result  = route(application, request).value

          status(result) mustEqual Status.SEE_OTHER
          redirectLocation(result).value mustEqual routes.NotificationConfirmationController
            .onPageLoad("notificationRef")
            .url

          verify(mockNotificationSubmitService, times(1)).getStateOfWorkItem(any())(using any())
        }
      }
    }

    "must return an exception" - {
      "when notificationSubmitService returns a failure" in {
        when(mockNotificationSubmitService.getStateOfWorkItem(any())(using any()))
          .thenReturn(Future.successful(NotificationState.Failure(500)))

        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.NotificationSubmittingController.onPageLoad("key").url)
          val result  = route(application, request).value

          intercept[InternalServerException] {
            await(result)
          }
          verify(mockNotificationSubmitService, times(1)).getStateOfWorkItem(any())(using any())
        }
      }
    }
  }
}
