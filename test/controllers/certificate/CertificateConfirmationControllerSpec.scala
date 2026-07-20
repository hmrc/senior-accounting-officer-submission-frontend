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
import navigation.{FakeNavigator, Navigator}
import play.api.http.HeaderNames
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.certificate.CertificateConfirmationView
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import scala.concurrent.Future
import uk.gov.hmrc.objectstore.client.ObjectListing
import java.time.Instant
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.ObjectSummary

class CertificateConfirmationControllerSpec extends SpecBase {

  def onwardRoute: Call = Call("GET", "/senior-accounting-officer/submission/certificate/task-list/complete")
  def certificateRef    = "SAOCRT0123456789"

  "CertificateConfirmation Controller" - {

    "onPageLoad" - {
      "must return OK and a view with no link displayed when object store finds no files" in {

        val mockObjectStoreClient = mock[PlayObjectStoreClient]
        when(
          mockObjectStoreClient.listObjects(
            path = any(),
            owner = any()
          )(using
            any()
          )
        )
          .thenReturn(Future.successful(ObjectListing(Nil)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[PlayObjectStoreClient].toInstance(mockObjectStoreClient))
          .build()

        running(application) {
          val request =
            FakeRequest(GET, certificateRoutes.CertificateConfirmationController.onPageLoad(certificateRef).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CertificateConfirmationView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(certificateRef, false)(using request, messages(application)).toString
        }
      }

      "must return OK and a view with a link displayed when object store finds a file" in {

        val mockObjectStoreClient = mock[PlayObjectStoreClient]
        when(
          mockObjectStoreClient.listObjects(
            path = any(),
            owner = any()
          )(using
            any()
          )
        )
          .thenReturn(
            Future.successful(
              ObjectListing(List(ObjectSummary(Path.File(Path.Directory("directory"), "fileName"), 0, Instant.now())))
            )
          )

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[PlayObjectStoreClient].toInstance(mockObjectStoreClient))
          .build()

        running(application) {
          val request =
            FakeRequest(GET, certificateRoutes.CertificateConfirmationController.onPageLoad(certificateRef).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CertificateConfirmationView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(certificateRef, true)(using request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(GET, certificateRoutes.CertificateConfirmationController.onPageLoad(certificateRef).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "must redirect to the next page for a POST" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
        .build()

      running(application) {
        val request = FakeRequest(POST, certificateRoutes.CertificateConfirmationController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        header(HeaderNames.LOCATION, result) mustEqual Some(onwardRoute.url)
      }
    }
  }
}
