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

package services

import models.*
import repositories.SessionRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject

class UpscanCallbackDispatcher @Inject() (sessionStorage: SessionRepository)(using ExecutionContext) {

  def processUpscanCallback(callback: UpscanCallback): Future[Boolean] = {
    println("jacobwozere")
    println(callback)
    val uploadStatus =
      callback match {
        case s: UpscanSuccessCallback =>
          UploadStatus.UploadedSuccessfully(
            name = s.uploadDetails.fileName,
            mimeType = s.uploadDetails.fileMimeType,
            downloadUrl = s.downloadUrl,
            size = Some(s.uploadDetails.size)
          )
        case UpscanFailureCallback(_, UpscanFailureDetails("QUARANTINE", _)) =>
          UploadStatus.Quarantined
        case UpscanFailureCallback(_, UpscanFailureDetails("REJECTED", _)) =>
          UploadStatus.Rejected
        case UpscanFailureCallback(_, UpscanFailureDetails(_, _)) =>
          UploadStatus.UnknownFailure
      }

    sessionStorage.updateUploadStatus(callback.reference, uploadStatus).map(_ => true)
  }

}
