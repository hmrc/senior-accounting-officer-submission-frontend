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

package services

import connectors.UpscanDownloadConnector
import models.UploadStatus.*
import models.{FileUploadState, UploadId, UpscanFileReference}
import play.api.http.Status.OK
import repositories.UpscanSessionRepository
import services.UpscanService.*
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class UpscanService @Inject() (
    repository: UpscanSessionRepository,
    downloadConnector: UpscanDownloadConnector
)(using ExecutionContext) {

  def fileUploadState(uploadId: String)(using hc: HeaderCarrier): Future[State] =
    checkMongo(uploadId).flatMap {
      _.fold(
        state => Future.successful(state),
        (reference, url) =>
          downloadConnector.download(url).map {
            case HttpResponse(OK, body, _) =>
              State.Result(reference, body)
            case httpResponse =>
              State.DownloadFromUpscanFailed(httpResponse)
          }
      )
    }

  private def checkMongo(uploadId: String): Future[Either[State, (UpscanFileReference, String)]] =
    repository.findByUploadId(UploadId(uploadId)).map {
      case Some(FileUploadState(_, _, _, InProgress, _)) =>
        Left(State.WaitingForUpscan)
      case Some(FileUploadState(_, _, _, UploadedSuccessfully(_, _, downloadUrl, _), _)) =>
        Right(downloadUrl)
      case Some(FileUploadState(_, _, _, Failed, _)) =>
      case Some(FileUploadState(_, _, reference, UploadedSuccessfully(_, _, downloadUrl, _))) =>
        Right((reference, downloadUrl))
      case Some(FileUploadState(_, _, _, Failed)) =>
        Left(State.UploadToUpscanFailed)
      case _ =>
        Left(State.NoUploadId)
    }
}

object UpscanService {
  enum State {
    case NoUploadId                                       extends State
    case WaitingForUpscan                                 extends State
    case UploadToUpscanFailed                             extends State
    case DownloadFromUpscanFailed(response: HttpResponse) extends State
    case Result(reference: UpscanFileReference, fileContent: String) extends State
  }
}
