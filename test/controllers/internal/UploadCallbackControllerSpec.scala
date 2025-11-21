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

package controllers.internal

import base.SpecBase
import models.*
import models.{UpscanFailureCallback, UpscanSuccessCallback}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.UpscanCallbackDispatcher

import scala.concurrent.Future

class UploadCallbackControllerSpec extends SpecBase with MockitoSugar {
  "UploadCallbackController" must {

    "handle a ReadyCallbackBody" in {
      val mockUpscanCallbackDispatcher = mock[UpscanCallbackDispatcher]
      val application                  = applicationBuilder()
        .overrides(
          bind[UpscanCallbackDispatcher].toInstance(mockUpscanCallbackDispatcher)
        )
        .build()

      running(application) {
        when(mockUpscanCallbackDispatcher.handleCallback(any())).thenReturn(Future.successful(()))

        val json = Json.parse(
          """
            |{
            |  "reference": "foo",
            |  "downloadUrl": "http://localhost:8080/download",
            |  "uploadDetails": {
            |    "uploadTimestamp": "2021-01-01T00:00:00Z",
            |    "checksum": "bar",
            |    "fileMimeType": "application/pdf",
            |    "fileName": "test.pdf",
            |    "size": 123
            |  },
            |  "fileStatus": "READY"
            |}
            |""".stripMargin
        )

        val request = FakeRequest(POST, routes.UploadCallbackController.callback().url).withJsonBody(json)

        val result = route(application, request).value

        status(result) mustEqual OK
        verify(mockUpscanCallbackDispatcher, times(1)).handleCallback(any[UpscanSuccessCallback])
      }
    }

    "handle a FailedCallbackBody" in {
      val mockUpscanCallbackDispatcher = mock[UpscanCallbackDispatcher]
      val application                  = applicationBuilder()
        .overrides(
          bind[UpscanCallbackDispatcher].toInstance(mockUpscanCallbackDispatcher)
        )
        .build()

      running(application) {
        when(mockUpscanCallbackDispatcher.handleCallback(any())).thenReturn(Future.successful(()))

        val json = Json.parse(
          """
            |{
            |  "reference": "foo",
            |  "failureDetails": {
            |    "failureReason": "QUARANTINE",
            |    "message": "This file has a virus"
            |  },
            |  "fileStatus": "FAILED"
            |}
            |""".stripMargin
        )

        val request = FakeRequest(POST, routes.UploadCallbackController.callback().url).withJsonBody(json)

        val result = route(application, request).value

        status(result) mustEqual OK
        verify(mockUpscanCallbackDispatcher, times(1)).handleCallback(any[UpscanFailureCallback])
      }
    }

    "return BadRequest for invalid json" in {
      val application = applicationBuilder().build()

      running(application) {
        val json = Json.parse(
          """
            |{
            |  "foo": "bar"
            |}
            |""".stripMargin
        )

        val request = FakeRequest(POST, routes.UploadCallbackController.callback().url).withJsonBody(json)

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
  }

}
