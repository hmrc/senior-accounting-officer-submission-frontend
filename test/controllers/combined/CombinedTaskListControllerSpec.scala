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

package controllers.combined

import base.SpecBase
import config.{AppConfig, FeatureToggleSupport}
import controllers.combined.routes as combinedRoutes
import models.*
import models.combined.CombinedStage
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.notification.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.combined.CombinedTaskListView

import java.time.LocalDate

class CombinedTaskListControllerSpec
    extends SpecBase
    with FeatureToggleSupport
    with BeforeAndAfterEach
    with GuiceOneAppPerSuite {

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    super.afterEach()
  }

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "CombinedTaskList Controller" - {

    "must return OK and the initial task list view for a GET with no completed tasks" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, combinedRoutes.CombinedTaskListController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CombinedTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CombinedStage.ProvideSaoDetails)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and unlock the upload task when SAO details are complete" in {
      val application = applicationBuilder(userAnswers = Some(completedSaoDetailsAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, combinedRoutes.CombinedTaskListController.onPageLoad.url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[CombinedTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CombinedStage.UploadSubmissionTemplateDetails)(using
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
        val request = FakeRequest(GET, combinedRoutes.CombinedTaskListController.onPageLoad.url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[CombinedTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CombinedStage.ProvideSaoDetails)(using
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
        val request = FakeRequest(GET, combinedRoutes.CombinedTaskListController.onPageLoad.url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[CombinedTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CombinedStage.ProvideSaoDetails)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and keep the upload task unlocked when the upload is complete but the review is not confirmed" in {
      val application = applicationBuilder(userAnswers = Some(completedNotificationUploadAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, combinedRoutes.CombinedTaskListController.onPageLoad.url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[CombinedTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CombinedStage.UploadSubmissionTemplateDetails)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and unlock the submit notification task when the upload and review confirmation are complete" in {
      val application = applicationBuilder(userAnswers = Some(completedNotificationReviewAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, combinedRoutes.CombinedTaskListController.onPageLoad.url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[CombinedTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CombinedStage.SubmitNotificationInfo)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and unlock the submit certificate task when the submit notification is complete" in {
      val application = applicationBuilder(userAnswers = Some(completedSubmittedNotificationAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, combinedRoutes.CombinedTaskListController.onPageLoad.url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[CombinedTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CombinedStage.SubmitCertificateInfo)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the completed task list view for a GET when all tasks is complete" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, combinedRoutes.CombinedTaskListController.onComplete.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CombinedTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CombinedStage.AllStagesCompleted)(using
          request,
          messages(application)
        ).toString
      }
    }
    "onSubmit" - {
      "when submission successful, must redirect to the home page" in {

        val application =
          applicationBuilder(userAnswers = Some(completedNotificationReviewAnswers)).build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              combinedRoutes.CombinedTaskListController.onCompleteSubmit.url
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual appConfig.hubBaseUrl
        }
      }
    }
  }
}
