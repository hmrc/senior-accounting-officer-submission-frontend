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

package controllers.notification

import base.SpecBase
import controllers.notification.routes as notificationRoutes
import controllers.routes
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.NotificationCheckYourAnswersService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.notification.NotificationCheckYourAnswersView
import services.NotificationSubmitService
import controllers.notification.NotificationCheckYourAnswersControllerSpec.*
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future
import services.NotificationSubmissionError

class NotificationCheckYourAnswersControllerSpec extends SpecBase {

  def onwardRoute: Call = Call("GET", "/foo")

  "NotificationCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {
      val mockService = mock[NotificationCheckYourAnswersService]
      when(mockService.getSummaryList(any())(using any())).thenReturn(SummaryList())

      val userAnswers = completedNotificationReviewAnswers
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[NotificationCheckYourAnswersService].toInstance(mockService))
        .build()

      running(application) {
        given request: Request[AnyContentAsEmpty.type] =
          FakeRequest(GET, notificationRoutes.NotificationCheckYourAnswersController.onPageLoad().url)

        val result     = route(application, request).value
        val view       = application.injector.instanceOf[NotificationCheckYourAnswersView]
        given Messages = messages(application)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(SummaryList(), userAnswers.getFinancialYearEndDate).toString
        verify(mockService).getSummaryList(meq(userAnswers))(using any())
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, notificationRoutes.NotificationCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "onSubmit" - {
      "when submission successful, must redirect to the notification confirmation page with the notification reference" in {

        val mockNotificationSubmitService = mock[NotificationSubmitService]

        when(mockNotificationSubmitService.submit(any())(using any[HeaderCarrier]()))
          .thenReturn(Future.successful(Right(exampleNotificationReference)))

        val application =
          applicationBuilder(userAnswers = Some(completedNotificationUploadAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[NotificationSubmitService].toInstance(mockNotificationSubmitService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, notificationRoutes.NotificationCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual notificationRoutes.NotificationConfirmationController
            .onPageLoad(
              exampleNotificationReference
            )
            .url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, notificationRoutes.NotificationCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "when submission unsuccessful, must throw an InternalServerException with an error message" in {

        val mockNotificationSubmitService = mock[NotificationSubmitService]

        when(mockNotificationSubmitService.submit(any())(using any[HeaderCarrier]()))
          .thenReturn(Future.successful(Left(NotificationSubmissionError.HttpError)))

        val application =
          applicationBuilder(userAnswers = Some(completedNotificationUploadAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[NotificationSubmitService].toInstance(mockNotificationSubmitService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, notificationRoutes.NotificationCheckYourAnswersController.onSubmit().url)

          // TODO: is this the right way of unit testing this?
          val exception = intercept[InternalServerException] {
            val result = route(application, request).value
            status(result)
          }
          exception.message mustEqual exampleErrorMessage
        }
      }
    }
  }
}

object NotificationCheckYourAnswersControllerSpec {
  val exampleNotificationReference = "example notification reference"
  val exampleErrorMessage          = "Problem with http client"
}
