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

package controllers.testonly

import base.SpecBase
import connectors.InternalAuthTestOnlyConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class InternalAuthTestOnlyControllerSpec extends SpecBase with MockitoSugar {

  "InternalAuthTestOnlyController.grantSaoObjectStoreAccess" - {
    "return the downstream internal-auth response status and body" in {
      val mockConnector = mock[InternalAuthTestOnlyConnector]
      val application   = applicationBuilder()
        .overrides(bind[InternalAuthTestOnlyConnector].toInstance(mockConnector))
        .build()

      running(application) {
        when(mockConnector.grantSaoObjectStoreAccess()(using any[HeaderCarrier]()))
          .thenReturn(Future.successful(HttpResponse(CREATED, "configured")))

        val request    = FakeRequest(POST, "/test-only/internal-auth/object-store")
        val controller = application.injector.instanceOf[InternalAuthTestOnlyController]

        val result = controller.grantSaoObjectStoreAccess()(request)

        status(result) mustBe CREATED
        contentAsString(result) mustBe "configured"
      }
    }
  }
}
