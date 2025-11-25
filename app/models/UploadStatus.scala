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

sealed trait UploadStatus:
  def fold[T](
      ifInProgress: => T,
      ifFailed: => T,
      ifSuccess: UploadStatus.UploadedSuccessfully => T
  ): T = this match {
    case UploadStatus.InProgress              => ifInProgress
    case UploadStatus.Failed                  => ifFailed
    case s: UploadStatus.UploadedSuccessfully => ifSuccess(s)
  }

object UploadStatus:
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
