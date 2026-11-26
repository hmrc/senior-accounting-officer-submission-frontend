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
import controllers.combined.routes as combinedRoutes
import controllers.combined.submitCertificate.routes as submitCertificateRoutes
import controllers.routes
import models.FeatureToggle.CombinedJourney
import models.JourneySection
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.PDFAccessService
import views.html.combined.submitCertificate.CombinedCertificateConfirmationView

import scala.concurrent.Future

class CombinedCertificateConfirmationControllerSpec extends SpecBase with BeforeAndAfterEach with FeatureToggleSupport {

  def onwardRoute: Call = Call("GET", "/senior-accounting-officer/submission/certificate/task-list/complete")
  def certificateRef    = "SAOCRT0123456789"

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(CombinedJourney)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(CombinedJourney)
  }

  "CombinedCertificateConfirmation Controller" - {

    "onPageLoad" - {
      "must return OK and a view with no link displayed when pdf is not available" in {

        val mockPDFAccessService = mock[PDFAccessService]
        when(
          mockPDFAccessService.checkPDFStatus(any(), any(), any())(using any())
        )
          .thenReturn(Future.successful(false))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[PDFAccessService].toInstance(mockPDFAccessService))
          .build()

        running(application) {
          val request =
            FakeRequest(
              GET,
              submitCertificateRoutes.CombinedCertificateConfirmationController.onPageLoad(certificateRef).url
            )

          val result = route(application, request).value

          val view = application.injector.instanceOf[CombinedCertificateConfirmationView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(certificateRef, false)(using request, messages(application)).toString
        }
      }

      "must return OK and a view with a link displayed when pdf is available" in {

        val mockPDFAccessService = mock[PDFAccessService]
        when(
          mockPDFAccessService.checkPDFStatus(any(), any(), any())(using any())
        )
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[PDFAccessService].toInstance(mockPDFAccessService))
          .build()

        running(application) {
          val request =
            FakeRequest(
              GET,
              submitCertificateRoutes.CombinedCertificateConfirmationController.onPageLoad(certificateRef).url
            )

          val result = route(application, request).value

          val view = application.injector.instanceOf[CombinedCertificateConfirmationView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(certificateRef, true)(using request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(
              GET,
              submitCertificateRoutes.CombinedCertificateConfirmationController.onPageLoad(certificateRef).url
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController
            .onPageLoad(section = Some(JourneySection.Combined))
            .url
        }
      }
    }

    "onSubmit" - {
      "must redirect to the combined task list page for a POST" in {
        val application = applicationBuilder(userAnswers = Some(completedSubmittedNotificationAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, submitCertificateRoutes.CombinedCertificateConfirmationController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual combinedRoutes.CombinedTaskListController.onComplete.url
        }
      }
    }
  }
}
