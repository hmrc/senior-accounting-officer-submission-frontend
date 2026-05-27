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
import models.SubmitNotificationStage
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.SubmitNotificationStartView

import scala.concurrent.Future

class SubmitNotificationStartControllerSpec extends SpecBase with MockitoSugar {

  "SubmitNotificationStart Controller" - {

    "must return OK and the initial task list view for a GET with no completed tasks" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(emptyUserAnswers)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmitNotificationStartController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmitNotificationStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(SubmitNotificationStage.ProvideSaoDetails)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and unlock the upload task when SAO details are complete" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(completedSaoDetailsAnswers)))

      val application = applicationBuilder(userAnswers = Some(completedSaoDetailsAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmitNotificationStartController.onPageLoad().url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[SubmitNotificationStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(SubmitNotificationStage.UploadSubmissionTemplateDetails)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and unlock the submit task when the upload is complete" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.get(userAnswersId))
        .thenReturn(Future.successful(Some(completedNotificationUploadAnswers)))

      val application = applicationBuilder(userAnswers = Some(completedNotificationUploadAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmitNotificationStartController.onPageLoad().url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[SubmitNotificationStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(SubmitNotificationStage.SubmitNotificationInfo)(using
          request,
          messages(application)
        ).toString
      }
    }
  }
}
