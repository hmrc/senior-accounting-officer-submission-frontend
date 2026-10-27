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
import config.AppConfig
import controllers.notification.routes as notificationRoutes
import models.NormalMode
import models.notification.NotificationStage
import pages.notification.*
import play.api.http.HeaderNames
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.notification.NotificationTaskListView

import java.time.LocalDate

class NotificationTaskListControllerSpec extends SpecBase {

  "NotificationTaskList Controller" - {

    val hubBaseUrl = "http://localhost:10056/senior-accounting-officer"

    "must return OK and the initial task list view for a GET with no completed tasks" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, notificationRoutes.NotificationTaskListController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NotificationTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NotificationStage.ProvideSaoDetails)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and unlock the upload task when SAO details are complete" in {
      val application = applicationBuilder(userAnswers = Some(completedSaoDetailsAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, notificationRoutes.NotificationTaskListController.onPageLoad().url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[NotificationTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NotificationStage.UploadSubmissionTemplateDetails)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and not unlock the upload task when SAO details are provided for one sao but prior answer indicates answers are related to multiple saos" in {
      val userAnswers = emptyUserAnswers
        .add(NotificationMoreThanOneSaoPage(NormalMode), true)
        .add(NotificationSingleSaoOfficerNamePage(NormalMode), "Jackson Brown")

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, notificationRoutes.NotificationTaskListController.onPageLoad().url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[NotificationTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NotificationStage.ProvideSaoDetails)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and not unlock the upload task when SAO details are provided for multiple saos but prior answer indicates answers are related to one sao" in {
      val userAnswers = emptyUserAnswers
        .add(NotificationMoreThanOneSaoPage(NormalMode), false)
        .add(NotificationMultiSaoLastOfficerNamePage(NormalMode), "Jackson Brown")
        .add(NotificationMultiSaoLastOfficerStartDatePage(NormalMode), LocalDate.of(2000, 1, 1))
        .add(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), LocalDate.of(2000, 1, 2))
        .add(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), LocalDate.of(2000, 1, 3))
        .add(NotificationMultiSaoAreAllAddedPage(0, NormalMode), true)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, notificationRoutes.NotificationTaskListController.onPageLoad().url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[NotificationTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NotificationStage.ProvideSaoDetails)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and keep the upload task unlocked when the upload is complete but the review is not confirmed" in {
      val application = applicationBuilder(userAnswers = Some(completedNotificationUploadAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, notificationRoutes.NotificationTaskListController.onPageLoad().url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[NotificationTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NotificationStage.UploadSubmissionTemplateDetails)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and unlock the submit task when the upload and review confirmation are complete" in {
      val application = applicationBuilder(userAnswers = Some(completedNotificationReviewAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, notificationRoutes.NotificationTaskListController.onPageLoad().url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[NotificationTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NotificationStage.SubmitNotificationInfo)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the completed task list view for a GET when all tasks is complete" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, notificationRoutes.NotificationTaskListController.onComplete().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NotificationTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NotificationStage.AllStagesCompleted)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the homepage for a completed task list POST" in {
      AppConfig.setValue("hub-frontend.host", "http://localhost:10056")

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, notificationRoutes.NotificationTaskListController.onCompleteSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        header(HeaderNames.LOCATION, result) mustEqual Some(hubBaseUrl)
      }
    }
  }
}
