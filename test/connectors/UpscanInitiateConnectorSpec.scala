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

package connectors

import base.SpecBase
import config.AppConfig
import models.{UpscanFileReference, UpscanInitiateResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UpscanInitiateConnectorSpec extends SpecBase with MockitoSugar {
  "UpscanInitiateConnector must" - {
    "initiateV2" in {
      val mockHttpClient     = mock[HttpClientV2]
      val mockAppConfig      = mock[AppConfig]
      val mockRequestBuilder = mock[RequestBuilder]
      val connector          = new UpscanInitiateConnector(mockHttpClient, mockAppConfig)

      val upscanInitiateResponse = UpscanInitiateResponse(
        fileReference = UpscanFileReference("foo"),
        postTarget = "bar",
        formFields = Map("baz1" -> "baz2")
      )

      when(mockAppConfig.initiateV2Url).thenReturn("http://localhost:8080/initiate")
      when(mockAppConfig.callbackEndpointTarget).thenReturn("http://localhost:8080/callback")
      when(mockHttpClient.post(any[java.net.URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[UpscanInitiateResponse](any(), any()))
        .thenReturn(Future.successful(upscanInitiateResponse))

      val result = connector.initiateV2("fakeUploadId")(using HeaderCarrier()).futureValue

      result mustBe an[UpscanInitiateResponse]
    }
  }
}
