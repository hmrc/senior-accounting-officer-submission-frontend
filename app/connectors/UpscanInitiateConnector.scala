/*
 * Copyright 2023 HM Revenue & Customs
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
import models.{UpscanFileReference, UpscanInitiateResponse}
import play.api.libs.json.*
import play.api.libs.ws.writeableOf_JsValue
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

sealed trait UpscanInitiateRequest

case class UpscanInitiateRequestV2(
    callbackUrl: String,
    successRedirect: Option[String] = None,
    errorRedirect: Option[String] = None,
    minimumFileSize: Option[Int] = None,
    maximumFileSize: Option[Int] = Some(4096)
) extends UpscanInitiateRequest

object UpscanInitiateRequestV2:
  given Format[UpscanInitiateRequestV2] = Json.format[UpscanInitiateRequestV2]

case class UploadForm(
    href: String,
    fields: Map[String, String]
)

case class Reference(value: String) extends AnyVal

object Reference:
  given Reads[Reference] = Reads.StringReads.map(Reference(_))

case class PreparedUpload(
    reference: Reference,
    uploadRequest: UploadForm
)

object PreparedUpload:
  given Reads[UploadForm]     = Json.reads[UploadForm]
  given Reads[PreparedUpload] = Json.reads[PreparedUpload]

class UpscanInitiateConnector @Inject() (
    httpClient: HttpClientV2,
    appConfig: AppConfig
)(using ExecutionContext):

  private val headers = Map(
    HeaderNames.CONTENT_TYPE -> "application/json"
  )

  def initiateV2(
      redirectOnSuccess: Option[String],
      redirectOnError: Option[String]
  )(using HeaderCarrier): Future[UpscanInitiateResponse] =
    val request = UpscanInitiateRequestV2(
      callbackUrl = appConfig.callbackEndpointTarget,
      successRedirect = redirectOnSuccess,
      errorRedirect = redirectOnError
    )
    initiate(appConfig.initiateV2Url, request)

  private def initiate[T](
      url: String,
      request: T
  )(using HeaderCarrier, Writes[T]): Future[UpscanInitiateResponse] =
    for
      response <- httpClient
        .post(url"$url")
        .withBody(Json.toJson(request))
        .setHeader(headers.toSeq*)
        .execute[PreparedUpload]
      fileReference = UpscanFileReference(response.reference.value)
      postTarget    = response.uploadRequest.href
      formFields    = response.uploadRequest.fields
    yield UpscanInitiateResponse(fileReference, postTarget, formFields)
