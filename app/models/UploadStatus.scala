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

import play.api.libs.json.*

sealed trait UploadStatus
object UploadStatus {

  case object InProgress extends UploadStatus
  case object Failed extends UploadStatus
  final case class UploadedSuccessfully(
      name: String,
      mimeType: String,
      downloadUrl: String,
      size: Option[Long]
  ) extends UploadStatus

  given OFormat[UploadStatus.UploadedSuccessfully] = Json.format[UploadStatus.UploadedSuccessfully]

  given Format[UploadStatus] = {
    val read: Reads[UploadStatus] = {
      case json:JsObject =>
        (json \ "statusType").validate[String].flatMap {
          case "InProgress" => JsSuccess(InProgress)
          case "Failed" => JsSuccess(Failed)
          case "UploadedSuccessfully" => json.validate[UploadedSuccessfully]
          case other => JsError(s"Unexpected statusType: $other")
        }
      case other => JsError(s"Expected a JsObject but got: ${other.getClass.getSimpleName}")
    }

    val write: Writes[UploadStatus] = Writes {
      case InProgress => Json.obj("statusType" -> "InProgress")
      case Failed => Json.obj("statusType" -> "Failed")
      case success: UploadedSuccessfully => Json.toJsObject(success) ++ Json.obj("statusType" -> "UploadedSuccessfully")
    }
    Format(read, write)
  }
}
