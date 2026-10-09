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
import services.CertificateSubmissionService
import services.CertificateSubmissionService.CertificateSubmissionResult
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.Future

class CertificateSubmittingControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val mockCertificateSubmitService: CertificateSubmissionService = mock[CertificateSubmissionService]

  override def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    super
      .applicationBuilder(Some(emptyUserAnswers))
      .overrides(
        inject.bind[CertificateSubmissionService].toInstance(mockCertificateSubmitService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCertificateSubmitService)
  }

  "CertificatePendingController.onPageLoad" - {
    "must return OK and the correct view" - {
      "when CertificateSubmissionService returns a pending result" in {
        when(mockCertificateSubmitService.getStateOfWorkItem(any())(using any()))
          .thenReturn(Future.successful(CertificateSubmissionResult.Pending("key")))

        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.CertificateSubmittingController.onPageLoad("key").url)
          val result  = route(application, request).value

          status(result) mustEqual Status.OK

          verify(mockCertificateSubmitService, times(1)).getStateOfWorkItem(any())(using any())
        }
      }
    }

    "must return a redirect to the certificate confirmation page" - {
      "when CertificateSubmissionService returns a submitted result" in {
        when(mockCertificateSubmitService.getStateOfWorkItem(any())(using any()))
          .thenReturn(Future.successful(CertificateSubmissionResult.Submitted("notificationRef")))

        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.CertificateSubmittingController.onPageLoad("key").url)
          val result  = route(application, request).value

          status(result) mustEqual Status.SEE_OTHER
          redirectLocation(result).value mustEqual routes.CertificateConfirmationController
            .onPageLoad("notificationRef")
            .url

          verify(mockCertificateSubmitService, times(1)).getStateOfWorkItem(any())(using any())
        }
      }
    }

    "must return an exception" - {
      "when notificationSubmitService returns a failure" in {
        when(mockCertificateSubmitService.getStateOfWorkItem(any())(using any()))
          .thenReturn(Future.successful(CertificateSubmissionResult.Failed))

        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.CertificateSubmittingController.onPageLoad("key").url)
          val result  = route(application, request).value

          intercept[InternalServerException] {
            await(result)
          }
          verify(mockCertificateSubmitService, times(1)).getStateOfWorkItem(any())(using any())
        }
      }
    }
  }
}
