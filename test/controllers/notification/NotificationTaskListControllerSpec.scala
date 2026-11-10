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
import config.FeatureToggleSupport
import controllers.notification.NotificationCheckYourAnswersControllerSpec.*
import controllers.notification.routes as notificationRoutes
import controllers.routes
import models.*
import models.FeatureToggle.FaultTolerance
import models.TaskStatus.{CannotStartYet, Completed, NotStarted}
import models.notification.NotificationStage
import models.notification.NotificationStage.{
  ProvideSaoDetailsStage,
  SubmitNotificationStage,
  UploadNotificationTemplateStage
}
import models.notification.NotificationSubmissionError
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.notification.*
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.NotificationSubmitService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}
import views.html.notification.NotificationTaskListView

import scala.concurrent.Future

import java.time.LocalDate

class NotificationTaskListControllerSpec extends SpecBase with FeatureToggleSupport with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(FaultTolerance)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(FaultTolerance)
  }

  "NotificationTaskList Controller" - {

    "must return OK and the initial task list view for a GET with no completed tasks" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request =
          FakeRequest(GET, notificationRoutes.NotificationTaskListController.onPageLoad(ProvideSaoDetailsStage).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NotificationTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          NotificationTaskListState(
            provideSaoDetailsStage = NotStarted,
            uploadNotificationTemplateStage = CannotStartYet,
            submitNotificationStage = CannotStartYet,
            showViewYourConfirmationButton = false
          )
        )(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and unlock the upload task when SAO details are complete" in {
      val application = applicationBuilder(userAnswers = Some(completedSaoDetailsAnswers))
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          notificationRoutes.NotificationTaskListController.onPageLoad(UploadNotificationTemplateStage).url
        )

        val result = route(application, request).value
        val view   = application.injector.instanceOf[NotificationTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          NotificationTaskListState(
            provideSaoDetailsStage = Completed,
            uploadNotificationTemplateStage = NotStarted,
            submitNotificationStage = CannotStartYet,
            showViewYourConfirmationButton = false
          )
        )(using
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
        val request =
          FakeRequest(GET, notificationRoutes.NotificationTaskListController.onPageLoad(ProvideSaoDetailsStage).url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[NotificationTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          NotificationTaskListState(
            provideSaoDetailsStage = NotStarted,
            uploadNotificationTemplateStage = CannotStartYet,
            submitNotificationStage = CannotStartYet,
            showViewYourConfirmationButton = false
          )
        )(using
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
        val request =
          FakeRequest(GET, notificationRoutes.NotificationTaskListController.onPageLoad(ProvideSaoDetailsStage).url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[NotificationTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          NotificationTaskListState(
            provideSaoDetailsStage = NotStarted,
            uploadNotificationTemplateStage = CannotStartYet,
            submitNotificationStage = CannotStartYet,
            showViewYourConfirmationButton = false
          )
        )(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and keep the upload task unlocked when the upload is complete but the review is not confirmed" in {
      val application = applicationBuilder(userAnswers = Some(completedNotificationUploadAnswers))
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          notificationRoutes.NotificationTaskListController.onPageLoad(UploadNotificationTemplateStage).url
        )

        val result = route(application, request).value
        val view   = application.injector.instanceOf[NotificationTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          NotificationTaskListState(
            provideSaoDetailsStage = Completed,
            uploadNotificationTemplateStage = NotStarted,
            submitNotificationStage = CannotStartYet,
            showViewYourConfirmationButton = false
          )
        )(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and unlock the submit task when the upload and review confirmation are complete" in {
      val application = applicationBuilder(userAnswers = Some(completedNotificationReviewAnswers))
        .build()

      running(application) {
        val request =
          FakeRequest(GET, notificationRoutes.NotificationTaskListController.onPageLoad(SubmitNotificationStage).url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[NotificationTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          NotificationTaskListState(
            provideSaoDetailsStage = Completed,
            uploadNotificationTemplateStage = Completed,
            submitNotificationStage = NotStarted,
            showViewYourConfirmationButton = false
          )
        )(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the completed task list view for a GET when all tasks is complete" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, notificationRoutes.NotificationTaskListController.onPageLoadComplete().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NotificationTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          NotificationTaskListState(
            provideSaoDetailsStage = Completed,
            uploadNotificationTemplateStage = Completed,
            submitNotificationStage = Completed,
            showViewYourConfirmationButton = true
          )
        )(using
          request,
          messages(application)
        ).toString
      }
    }
    "onSubmit" - {
      "when submission successful, must redirect to the notification confirmation page with the notification reference - fault tolerance disabled" in {
        disable(FaultTolerance)

        val mockNotificationSubmitService = mock[NotificationSubmitService]

        when(mockNotificationSubmitService.submit(any())(using any[HeaderCarrier]()))
          .thenReturn(Future.successful(Right(exampleNotificationReference)))

        val application =
          applicationBuilder(userAnswers = Some(completedNotificationReviewAnswers))
            .overrides(
              bind[NotificationSubmitService].toInstance(mockNotificationSubmitService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              notificationRoutes.NotificationTaskListController.onCompleteSubmit().url
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual notificationRoutes.NotificationConfirmationController
            .onPageLoad(exampleNotificationReference)
            .url
        }
      }

      "when submission successful, must redirect to the notification confirmation page with the notification reference - fault tolerance enabled" in {
        enable(FaultTolerance)

        val mockNotificationSubmitService = mock[NotificationSubmitService]

        when(mockNotificationSubmitService.submitWithFaultTolerance(any())(using any[HeaderCarrier]()))
          .thenReturn(Future.successful(Right("key")))

        val application =
          applicationBuilder(userAnswers = Some(completedNotificationReviewAnswers))
            .overrides(
              bind[NotificationSubmitService].toInstance(mockNotificationSubmitService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              notificationRoutes.NotificationTaskListController.onCompleteSubmit().url
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual notificationRoutes.NotificationSubmittingController
            .onPageLoad(
              "key"
            )
            .url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              notificationRoutes.NotificationTaskListController.onCompleteSubmit().url
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController
            .onPageLoad(section = Some(JourneySection.Notification))
            .url
        }
      }

      "when submission unsuccessful, must throw an InternalServerException with an error message - fault tolerance disabled" in {
        disable(FaultTolerance)

        val mockNotificationSubmitService = mock[NotificationSubmitService]

        when(mockNotificationSubmitService.submit(any())(using any[HeaderCarrier]()))
          .thenReturn(
            Future.successful(
              Left(NotificationSubmissionError.HttpError(HttpResponse(INTERNAL_SERVER_ERROR)))
            )
          )

        val application =
          applicationBuilder(userAnswers = Some(completedNotificationReviewAnswers))
            .overrides(
              bind[NotificationSubmitService].toInstance(mockNotificationSubmitService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              notificationRoutes.NotificationTaskListController.onCompleteSubmit().url
            )

          val exception = intercept[InternalServerException] {
            val result = route(application, request).value
            status(result)
          }
          exception.message mustEqual expectedHttpFailureMessage
        }
      }

      "when submission unsuccessful, must throw an InternalServerException with an error message - fault tolerance enabled" in {
        enable(FaultTolerance)

        val mockNotificationSubmitService = mock[NotificationSubmitService]

        when(mockNotificationSubmitService.submitWithFaultTolerance(any())(using any[HeaderCarrier]()))
          .thenReturn(
            Future.successful(
              Left(NotificationSubmissionError.HttpError(HttpResponse(INTERNAL_SERVER_ERROR)))
            )
          )

        val application =
          applicationBuilder(userAnswers = Some(completedNotificationReviewAnswers))
            .overrides(
              bind[NotificationSubmitService].toInstance(mockNotificationSubmitService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              notificationRoutes.NotificationTaskListController.onCompleteSubmit().url
            )

          val exception = intercept[InternalServerException] {
            val result = route(application, request).value
            status(result)
          }
          exception.message mustEqual expectedHttpFailureMessage
        }
      }
    }
  }
}
