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

package controllers.combined.submitCertificate

import base.SpecBase
import config.FeatureToggleSupport
import controllers.combined.submitCertificate.routes as submitCertificateRoutes
import controllers.routes
import models.FeatureToggle.FaultTolerance
import models.JourneySection
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.*
import services.CertificateSubmissionService.CertificateSubmissionResult
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.combined.submitCertificate.CombinedCertificateCheckYourAnswersView

import scala.concurrent.Future

class CombinedCertificateCheckYourAnswersControllerSpec
    extends SpecBase
    with MockitoSugar
    with FeatureToggleSupport
    with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(FaultTolerance)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(FaultTolerance)
  }

  "CombinedCertificateCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {
      val mockCheckYourAnswersService = mock[CertificateCheckYourAnswersService]
      val mockUserAnswersService      = mock[DeclarationUserAnswersService]

      when(mockCheckYourAnswersService.getSummaryList(any())(using any())).thenReturn(SummaryList())

      val testAnswers = completedSubmittedNotificationAnswers

      when(mockUserAnswersService.sanitise(any())).thenReturn(testAnswers)

      val application = applicationBuilder(userAnswers = Some(testAnswers))
        .overrides(bind[CertificateCheckYourAnswersService].toInstance(mockCheckYourAnswersService))
        .overrides(bind[DeclarationUserAnswersService].toInstance(mockUserAnswersService))
        .build()

      running(application) {
        val request =
          FakeRequest(GET, submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view     = application.injector.instanceOf[CombinedCertificateCheckYourAnswersView]
        val document = Jsoup.parse(contentAsString(result))
        val token    = document.select("input[name=certificateSubmissionToken]").attr("value")

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(SummaryList(), token)(using request, messages(application)).toString
        verify(mockCheckYourAnswersService).getSummaryList(meq(testAnswers))(using any())
      }
    }

    "must redirect to the next page for a POST" in {

      val application =
        applicationBuilder(userAnswers = Some(completedSubmittedNotificationAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController
          .onPageLoad(section = Some(JourneySection.Combined))
          .url
      }
    }

    "must submit the certificate and redirect to certificate confirmation for a POST - fault tolerance disabled" in {
      disable(FaultTolerance)
      val mockSubmissionService = mock[CertificateSubmissionService]
      val certificateRef        = "CRT0123456789"

      when(mockSubmissionService.submit(meq("id"), any(), meq("token"), any())(using any()))
        .thenReturn(Future.successful(CertificateSubmissionResult.Submitted(certificateRef)))

      val application =
        applicationBuilder(userAnswers = Some(completedSubmittedNotificationAnswers))
          .overrides(bind[CertificateSubmissionService].toInstance(mockSubmissionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onSubmit().url)
            .withFormUrlEncodedBody("certificateSubmissionToken" -> "token")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual submitCertificateRoutes.CombinedCertificateConfirmationController
          .onPageLoad(certificateRef)
          .url
      }
    }

    "must submit the certificate and redirect to certificate task list for a POST - fault tolerance enabled" in {
      enable(FaultTolerance)
      val mockSubmissionService = mock[CertificateSubmissionService]

      when(mockSubmissionService.submitWithFaultTolerance(meq("id"), any(), meq("token"), any())(using any()))
        .thenReturn(Future.successful(CertificateSubmissionResult.Pending("key")))

      val application =
        applicationBuilder(userAnswers = Some(completedSubmittedNotificationAnswers))
          .overrides(bind[CertificateSubmissionService].toInstance(mockSubmissionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onSubmit().url)
            .withFormUrlEncodedBody("certificateSubmissionToken" -> "token")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual submitCertificateRoutes.CombinedCertificateSubmittingController
          .onPageLoad("key")
          .url
      }
    }

    "must fail when submission fails so the error handler can render the 500 page - fault tolerance disabled" in {
      disable(FaultTolerance)
      val mockSubmissionService = mock[CertificateSubmissionService]

      when(mockSubmissionService.submit(meq("id"), any(), meq("token"), any())(using any()))
        .thenReturn(Future.successful(CertificateSubmissionResult.Failed))

      val application =
        applicationBuilder(userAnswers = Some(completedSubmittedNotificationAnswers))
          .overrides(bind[CertificateSubmissionService].toInstance(mockSubmissionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onSubmit().url)
            .withFormUrlEncodedBody("certificateSubmissionToken" -> "token")

        val result = route(application, request).value

        result.failed.futureValue mustBe CombinedCertificateCheckYourAnswersController.SubmissionFailedException
      }
    }

    "must fail when submission fails so the error handler can render the 500 page - fault tolerance enabled" in {
      enable(FaultTolerance)
      val mockSubmissionService = mock[CertificateSubmissionService]

      when(mockSubmissionService.submitWithFaultTolerance(meq("id"), any(), meq("token"), any())(using any()))
        .thenReturn(Future.successful(CertificateSubmissionResult.Failed))

      val application =
        applicationBuilder(userAnswers = Some(completedSubmittedNotificationAnswers))
          .overrides(bind[CertificateSubmissionService].toInstance(mockSubmissionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onSubmit().url)
            .withFormUrlEncodedBody("certificateSubmissionToken" -> "token")

        val result = route(application, request).value

        result.failed.futureValue mustBe CombinedCertificateCheckYourAnswersController.SubmissionFailedException
      }
    }

    "must redirect to Journey Recovery when required submission data is missing - fault tolerance disabled" in {
      disable(FaultTolerance)
      val mockSubmissionService = mock[CertificateSubmissionService]

      when(mockSubmissionService.submit(meq("id"), any(), meq("token"), any())(using any()))
        .thenReturn(Future.successful(CertificateSubmissionResult.MissingData))

      val application =
        applicationBuilder(userAnswers = Some(completedSubmittedNotificationAnswers))
          .overrides(bind[CertificateSubmissionService].toInstance(mockSubmissionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onSubmit().url)
            .withFormUrlEncodedBody("certificateSubmissionToken" -> "token")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController
          .onPageLoad(section = Some(JourneySection.Combined))
          .url
      }
    }

    "must redirect to Journey Recovery when required submission data is missing - fault tolerance enabled" in {
      enable(FaultTolerance)
      val mockSubmissionService = mock[CertificateSubmissionService]

      when(mockSubmissionService.submitWithFaultTolerance(meq("id"), any(), meq("token"), any())(using any()))
        .thenReturn(Future.successful(CertificateSubmissionResult.MissingData))

      val application =
        applicationBuilder(userAnswers = Some(completedSubmittedNotificationAnswers))
          .overrides(bind[CertificateSubmissionService].toInstance(mockSubmissionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onSubmit().url)
            .withFormUrlEncodedBody("certificateSubmissionToken" -> "token")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController
          .onPageLoad(section = Some(JourneySection.Combined))
          .url
      }
    }

    "must redirect back to check your answers when the submission token has already been used - fault tolerance disabled" in {
      disable(FaultTolerance)
      val mockSubmissionService = mock[CertificateSubmissionService]

      when(mockSubmissionService.submit(meq("id"), any(), meq("token"), any())(using any()))
        .thenReturn(Future.successful(CertificateSubmissionResult.Duplicate))

      val application =
        applicationBuilder(userAnswers = Some(completedSubmittedNotificationAnswers))
          .overrides(bind[CertificateSubmissionService].toInstance(mockSubmissionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onSubmit().url)
            .withFormUrlEncodedBody("certificateSubmissionToken" -> "token")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual submitCertificateRoutes.CombinedCertificateCheckYourAnswersController
          .onPageLoad()
          .url
      }
    }

    "must redirect back to check your answers when the submission token has already been used - fault tolerance enabled" in {
      enable(FaultTolerance)
      val mockSubmissionService = mock[CertificateSubmissionService]

      when(mockSubmissionService.submitWithFaultTolerance(meq("id"), any(), meq("token"), any())(using any()))
        .thenReturn(Future.successful(CertificateSubmissionResult.Duplicate))

      val application =
        applicationBuilder(userAnswers = Some(completedSubmittedNotificationAnswers))
          .overrides(bind[CertificateSubmissionService].toInstance(mockSubmissionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onSubmit().url)
            .withFormUrlEncodedBody("certificateSubmissionToken" -> "token")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual submitCertificateRoutes.CombinedCertificateCheckYourAnswersController
          .onPageLoad()
          .url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(GET, submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController
          .onPageLoad(section = Some(JourneySection.Combined))
          .url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController
          .onPageLoad(section = Some(JourneySection.Combined))
          .url
      }
    }

  }
}
