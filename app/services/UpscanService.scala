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
import models.FileUploadState
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

  def fileUploadState(reference: String)(using hc: HeaderCarrier): Future[State] =
    checkMongo(reference).flatMap {
      _.fold(
        state => Future.successful(state),
        { case InterimResult(reference, url) =>
          downloadConnector.download(url).map {
            case HttpResponse(OK, body, _) =>
              State.Result(reference, body)
            case httpResponse =>
              State.DownloadFromUpscanFailed(httpResponse)
          }
        }
      )
    }

  private def checkMongo(reference: String): Future[Either[State, InterimResult]] =
    repository.find(reference).map {
      case Some(FileUploadState(_, _, InProgress, _)) =>
        Left(State.WaitingForUpscan)
      case Some(FileUploadState(_, reference, UploadedSuccessfully(_, _, downloadUrl, _), _)) =>
        Right(InterimResult(reference, downloadUrl))
      case Some(FileUploadState(_, _, Failed, _)) =>
        Left(State.UploadToUpscanFailed)
      case _ =>
        Left(State.NoReference)
    }
}

object UpscanService {

  private final case class InterimResult(reference: String, fileContent: String)

  enum State {
    case NoReference                                      extends State
    case WaitingForUpscan                                 extends State
    case UploadToUpscanFailed                             extends State
    case DownloadFromUpscanFailed(response: HttpResponse) extends State
    case Result(reference: String, fileContent: String)   extends State
  }
}
