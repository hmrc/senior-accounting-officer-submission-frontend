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

package controllers.certificate

import base.SpecBase
import controllers.certificate.routes as certificateRoutes
import controllers.routes
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.*
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.CertificateCheckYourAnswersService
import services.CertificateSubmissionService
import services.CertificateSubmissionService.CertificateSubmissionResult
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.certificate.CertificateCheckYourAnswersView

import scala.concurrent.Future

class CertificateCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

  "CertificateCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {
      val mockService = mock[CertificateCheckYourAnswersService]

      when(mockService.getSummaryList(any())(using any())).thenReturn(SummaryList())

      val testAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(testAnswers))
        .overrides(bind[CertificateCheckYourAnswersService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, certificateRoutes.CertificateCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CertificateCheckYourAnswersView]
        val document = Jsoup.parse(contentAsString(result))
        val token = document.select("input[name=certificateSubmissionToken]").attr("value")

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(SummaryList(), token)(using request, messages(application)).toString
        verify(mockService).getSummaryList(meq(testAnswers))(using any())
      }
    }

    "must redirect to the next page for a POST" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, certificateRoutes.CertificateCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must submit the certificate and redirect to confirmation for a POST" in {
      val mockSubmissionService = mock[CertificateSubmissionService]

      when(mockSubmissionService.submit(meq("id"), meq("SAOSUB123456789"), any(), meq("token"))(using any()))
        .thenReturn(Future.successful(CertificateSubmissionResult.Submitted("CRT0123456789")))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[CertificateSubmissionService].toInstance(mockSubmissionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, certificateRoutes.CertificateCheckYourAnswersController.onSubmit().url)
            .withFormUrlEncodedBody("certificateSubmissionToken" -> "token")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual certificateRoutes.CertificateConfirmationController
          .onPageLoad("CRT0123456789")
          .url
      }
    }

    "must return internal server error when submission fails" in {
      val mockSubmissionService = mock[CertificateSubmissionService]

      when(mockSubmissionService.submit(meq("id"), meq("SAOSUB123456789"), any(), meq("token"))(using any()))
        .thenReturn(Future.successful(CertificateSubmissionResult.Failed))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[CertificateSubmissionService].toInstance(mockSubmissionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, certificateRoutes.CertificateCheckYourAnswersController.onSubmit().url)
            .withFormUrlEncodedBody("certificateSubmissionToken" -> "token")

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }

    "must redirect to Journey Recovery when required submission data is missing" in {
      val mockSubmissionService = mock[CertificateSubmissionService]

      when(mockSubmissionService.submit(meq("id"), meq("SAOSUB123456789"), any(), meq("token"))(using any()))
        .thenReturn(Future.successful(CertificateSubmissionResult.MissingData))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[CertificateSubmissionService].toInstance(mockSubmissionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, certificateRoutes.CertificateCheckYourAnswersController.onSubmit().url)
            .withFormUrlEncodedBody("certificateSubmissionToken" -> "token")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect back to check your answers when the submission token has already been used" in {
      val mockSubmissionService = mock[CertificateSubmissionService]

      when(mockSubmissionService.submit(meq("id"), meq("SAOSUB123456789"), any(), meq("token"))(using any()))
        .thenReturn(Future.successful(CertificateSubmissionResult.Duplicate))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[CertificateSubmissionService].toInstance(mockSubmissionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, certificateRoutes.CertificateCheckYourAnswersController.onSubmit().url)
            .withFormUrlEncodedBody("certificateSubmissionToken" -> "token")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual certificateRoutes.CertificateCheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, certificateRoutes.CertificateCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, certificateRoutes.CertificateCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
