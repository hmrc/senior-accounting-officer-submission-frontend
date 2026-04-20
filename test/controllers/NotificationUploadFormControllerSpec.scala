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
import connectors.UpscanInitiateConnector
import models.{NotificationUploadState, UploadStatus, UpscanInitiateResponse}
import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import views.html.NotificationUploadFormView

import scala.concurrent.{ExecutionContext, Future}

class NotificationUploadFormControllerSpec extends SpecBase with MockitoSugar {

  "NotificationUploadFormController must" - {

    "return OK and the correct view for a GET" in {
      val mockUpscanInitiateConnector    = mock[UpscanInitiateConnector]
      val mockSessionRepository          = mock[SessionRepository]
      val mockNotificationUploadFormView = mock[NotificationUploadFormView]

      val upscanInitiateResponse =
        UpscanInitiateResponse(reference = "foo", postTarget = "bar", formFields = Map("foo2" -> "foo2Val"))

      when(mockNotificationUploadFormView.apply(any(), any())(using any(), any())).thenReturn(Html(""))

      when(
        mockUpscanInitiateConnector.initiateV2()(using
          any[HeaderCarrier]()
        )
      ).thenReturn(Future.successful(upscanInitiateResponse))

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[UpscanInitiateConnector].toInstance(mockUpscanInitiateConnector),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[NotificationUploadFormView].toInstance(mockNotificationUploadFormView)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.NotificationUploadFormController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK

        verify(mockNotificationUploadFormView, times(1)).apply(any(), any())(using any(), any())
      }
    }

    "return an error when upscan initiate connector fails" in {
      given ec: ExecutionContext         = scala.concurrent.ExecutionContext.Implicits.global
      val mockUpscanInitiateConnector    = mock[UpscanInitiateConnector]
      val mockSessionRepository          = mock[SessionRepository]
      val mockNotificationUploadFormView = mock[NotificationUploadFormView]

      when(
        mockUpscanInitiateConnector.initiateV2()(using
          any[HeaderCarrier]
        )
      ).thenReturn(Future.failed(new RuntimeException("Upscan service unavailable")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[UpscanInitiateConnector].toInstance(mockUpscanInitiateConnector),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[NotificationUploadFormView].toInstance(mockNotificationUploadFormView)
        )
        .build()

      running(application) {

        val request = FakeRequest(GET, routes.NotificationUploadFormController.onPageLoad().url)

        val result = route(application, request).value

        result.failed.futureValue mustBe an[RuntimeException]

      }

    }

    "return an error when upload progress tracker fails" in {
      val mockUpscanInitiateConnector    = mock[UpscanInitiateConnector]
      val mockSessionRepository          = mock[SessionRepository]
      val mockNotificationUploadFormView = mock[NotificationUploadFormView]

      val upscanInitiateResponse =
        UpscanInitiateResponse(reference = "foo", postTarget = "bar", formFields = Map("foo2" -> "foo2Val"))

      when(
        mockUpscanInitiateConnector.initiateV2()(using
          any[HeaderCarrier]()
        )
      )
        .thenReturn(Future.successful(upscanInitiateResponse))

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.failed(new RuntimeException("Database connection failed")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[UpscanInitiateConnector].toInstance(mockUpscanInitiateConnector),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[NotificationUploadFormView].toInstance(mockNotificationUploadFormView)
        )
        .build()

      running(application) {

        val request = FakeRequest(GET, routes.NotificationUploadFormController.onPageLoad().url)

        val result = route(application, request).value

        result.failed.futureValue mustBe an[RuntimeException]

      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.NotificationUploadFormController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "store the Upscan reference in user answers as soon as the upload is initiated" in {
      val mockUpscanInitiateConnector    = mock[UpscanInitiateConnector]
      val mockSessionRepository          = mock[SessionRepository]
      val mockNotificationUploadFormView = mock[NotificationUploadFormView]

      val upscanInitiateResponse =
        UpscanInitiateResponse(reference = "foo", postTarget = "bar", formFields = Map("foo2" -> "foo2Val"))

      when(mockNotificationUploadFormView.apply(any(), any())(using any(), any())).thenReturn(Html(""))
      when(mockUpscanInitiateConnector.initiateV2()(using any[HeaderCarrier]()))
        .thenReturn(Future.successful(upscanInitiateResponse))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[UpscanInitiateConnector].toInstance(mockUpscanInitiateConnector),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[NotificationUploadFormView].toInstance(mockNotificationUploadFormView)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.NotificationUploadFormController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK

        verify(mockSessionRepository, times(1)).set(
          argThat { (answers: models.UserAnswers) =>
            answers.id == emptyUserAnswers.id &&
            answers
              .get(pages.NotificationUploadStatePage)
              .contains(
                NotificationUploadState(reference = "foo", status = UploadStatus.InProgress)
              )
          }
        )
      }
    }
  }
}
