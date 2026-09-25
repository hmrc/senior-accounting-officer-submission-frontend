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
import controllers.notification.NotificationSubmissionInProgressControllerSpec.*
import models.UserAnswers
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.notification.SubmissionInProgress
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.NotificationSubmitService
import uk.gov.hmrc.http.InternalServerException
import views.html.notification.NotificationSubmissionInProgressView

import scala.concurrent.Future

import java.util.UUID

class NotificationSubmissionInProgressControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  val mockNotificationSubmitService: NotificationSubmitService = mock[NotificationSubmitService]

  override protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    super
      .applicationBuilder(userAnswers)
      .overrides(
        inject.bind[NotificationSubmitService].toInstance(mockNotificationSubmitService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockNotificationSubmitService)
  }

  "NotificationSubmissionInProgress Controller" - {

    "when the user answer is missing the submission correlationId must throw an exception" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.NotificationSubmissionInProgressController.onPageLoad().url)

        val result = route(application, request).value

        val e = intercept[InternalServerException] {
          await(result)
        }
        e.message must include("[GetSubmissionStatus] The correlationId for the submission is not found in Mongo")
      }
    }

    "when getSubmissionStatus returns Right(Some(id)) must return Redirect to confirmation page" in {
      val userAnswers = emptyUserAnswers.set(SubmissionInProgress, testCorrelationId).get
      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()
      when(mockNotificationSubmitService.getSubmissionStatus(any())(using any())).thenReturn(
        Future.successful(Right(Some(testSubmissionId)))
      )

      running(application) {
        val request = FakeRequest(GET, routes.NotificationSubmissionInProgressController.onPageLoad().url)

        val result = route(application, request).value

        application.injector.instanceOf[NotificationSubmissionInProgressView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustBe routes.NotificationConfirmationController.onPageLoad(testSubmissionId).url
        verify(mockNotificationSubmitService).getSubmissionStatus(meq(testCorrelationId))(using any())
      }
    }

    "when getSubmissionStatus returns Right(None) must return OK and the correct view" in {
      val userAnswers = emptyUserAnswers.set(SubmissionInProgress, testCorrelationId).get
      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()
      when(mockNotificationSubmitService.getSubmissionStatus(any())(using any())).thenReturn(
        Future.successful(Right(None))
      )

      running(application) {
        val request = FakeRequest(GET, routes.NotificationSubmissionInProgressController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NotificationSubmissionInProgressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(using request, messages(application)).toString
        verify(mockNotificationSubmitService).getSubmissionStatus(meq(testCorrelationId))(using any())
      }
    }
  }

}

object NotificationSubmissionInProgressControllerSpec {
  val testCorrelationId: String = UUID.randomUUID().toString
  val testSubmissionId: String  = "test-submission-id"
}
