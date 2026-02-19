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

sealed trait UploadStatus {
  def fold[T](
      ifInProgress: => T,
      ifFailed: => T,
      ifSuccess: UploadStatus.UploadedSuccessfully => T
  ): T = this match {
    case UploadStatus.InProgress              => ifInProgress
    case UploadStatus.Failed                  => ifFailed
    case s: UploadStatus.UploadedSuccessfully => ifSuccess(s)
  }
}
object UploadStatus {

  /** The file upload is currently in progress
    */
  case object InProgress extends UploadStatus

  /** The file upload has failed
    */
  case object Failed extends UploadStatus

  /** The file has been successfully uploaded
    */
  final case class UploadedSuccessfully(
      name: String,
      mimeType: String,
      downloadUrl: String,
      size: Option[Long]
  ) extends UploadStatus

  private val statusType           = "statusType"
  private val inProgress           = "InProgress"
  private val failed               = "Failed"
  private val uploadedSuccessfully = "UploadedSuccessfully"

  given Format[UploadStatus.UploadedSuccessfully] = Json.format[UploadStatus.UploadedSuccessfully]

  given Format[UploadStatus] = {

    val read: Reads[UploadStatus] = {
      case jsObject: JsObject =>
        jsObject.value.get(statusType) match
          case Some(JsString(`inProgress`))           => JsSuccess(UploadStatus.InProgress)
          case Some(JsString(`failed`))               => JsSuccess(UploadStatus.Failed)
          case Some(JsString(`uploadedSuccessfully`)) => Json.fromJson[UploadStatus.UploadedSuccessfully](jsObject)
          case Some(value)                            => JsError(s"Unexpected value of statusType: $value")
          case None                                   => JsError("Missing statusType field")
      case other => JsError(s"Expected a JsObject but got ${other.getClass.getSimpleName}")
    }

    val write: Writes[UploadStatus] = { (p: UploadStatus) =>
      p fold (
        ifInProgress = JsObject(Map(statusType -> JsString(`inProgress`))),
        ifFailed = JsObject(Map(statusType -> JsString(`failed`))),
        ifSuccess = s =>
          Json.toJson(s).as[JsObject]
            + (statusType -> JsString(`uploadedSuccessfully`))
      )
    }

    Format(read, write)
  }
}
