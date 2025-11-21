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

package models

import connectors.Reference
import play.api.libs.json.*
import utils.HttpUrlFormat

import java.net.URL
import java.time.Instant

sealed trait UpscanCallback:
  def reference: Reference

final case class UpscanSuccessCallback(
    reference: Reference,
    downloadUrl: URL,
    uploadDetails: UpscanUploadDetails
) extends UpscanCallback

final case class UpscanFailureCallback(
    reference: Reference,
    failureDetails: UpscanFailureDetails
) extends UpscanCallback

object UpscanCallback:

  given Reads[UpscanUploadDetails]  = Json.reads[UpscanUploadDetails]
  given Reads[UpscanFailureDetails] = Json.reads[UpscanFailureDetails]

  given Reads[UpscanSuccessCallback] =
    given Format[URL] = HttpUrlFormat.format
    Json.reads[UpscanSuccessCallback]

  given Reads[UpscanFailureCallback] = Json.reads[UpscanFailureCallback]

  given Reads[UpscanCallback] =
    (json: JsValue) =>
      json \ "fileStatus" match
        case JsDefined(JsString("READY"))  => json.validate[UpscanSuccessCallback]
        case JsDefined(JsString("FAILED")) => json.validate[UpscanFailureCallback]
        case JsDefined(value)              => JsError(s"Invalid type discriminator: $value")
        case _                             => JsError(s"Missing type discriminator")

final case class UpscanUploadDetails(
    uploadTimestamp: Instant,
    checksum: String,
    fileMimeType: String,
    fileName: String,
    size: Long
)

final case class UpscanFailureDetails(
    failureReason: String,
    message: String
)
