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

import config.AppConfig
import models.{UpscanInitiateRequestV2, UpscanInitiateResponse}
import play.api.libs.json.*
import play.api.libs.ws.writeableOf_JsValue
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class UpscanInitiateConnector @Inject() (
    httpClient: HttpClientV2,
    appConfig: AppConfig
)(using ExecutionContext):

  private val headers = Map(
    HeaderNames.CONTENT_TYPE -> "application/json"
  )

  def initiateV2(uploadId: String)(using HeaderCarrier): Future[UpscanInitiateResponse] =
    val request = UpscanInitiateRequestV2(
      callbackUrl = appConfig.callbackEndpointTarget,
      successRedirect = Some(appConfig.hubBaseUrl + s"?uploadId=$uploadId"),
      errorRedirect = Some(appConfig.hubBaseUrl)
    )
    initiate(appConfig.initiateV2Url, request)

  private def initiate[T](
      url: String,
      request: T
  )(using HeaderCarrier, Writes[T]): Future[UpscanInitiateResponse] =
    httpClient
      .post(url"$url")
      .withBody(Json.toJson(request))
      .setHeader(headers.toSeq*)
      .execute[UpscanInitiateResponse]
