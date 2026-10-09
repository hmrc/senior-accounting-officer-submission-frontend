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

import connectors.ProtectedServiceConnector
import models.NormalMode
import models.UserAnswers
import models.notification.*
import models.upload.UploadTemplateTableData
import pages.notification.*
import play.api.http.Status.{ACCEPTED, NO_CONTENT, OK}
import play.api.libs.json.Json
import repositories.SessionRepository
import services.NotificationSubmitService.*
import services.NotificationSubmitService.NotificationState.Pending
import uk.gov.hmrc.http.HeaderCarrier

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

import java.util.UUID
import javax.inject.Inject

class NotificationSubmitService @Inject() (
    protectedServiceConnector: ProtectedServiceConnector,
    sessionRepository: SessionRepository
)(using
    ec: ExecutionContext
) {
  def submit(userAnswers: UserAnswers)(using HeaderCarrier): Future[Either[NotificationSubmissionError, String]] = {
    protectedServiceConnector
      .postNotification(userAnswers.toNotification(None))
      .flatMap { response =>
        response.status match {
          case OK => {
            val notificationReference = Json.parse(response.body).as[NotificationResponse].notificationRef
            sessionRepository
              .set(userAnswers.copy(data = Json.obj()))
              .map(_ => Right(notificationReference))
          }
          case _ => Future.successful(Left(NotificationSubmissionError.HttpError(response)))
        }
      }
  }

  def submitWithFaultTolerance(
      userAnswers: UserAnswers
  )(using HeaderCarrier): Future[Either[NotificationSubmissionError, String]] = {
    protectedServiceConnector
      .postNotificationWithFaultTolerance(userAnswers.toNotification(Some(generateIdempotencyKey)))
      .flatMap { response =>
        response.status match {
          case ACCEPTED =>
            val idempotencyKey = Json.parse(response.body).as[NotificationFaultToleranceResponse].idempotencyKey
            sessionRepository
              .set(userAnswers.copy(data = Json.obj()))
              .map(_ => Right(idempotencyKey))
          case _ => Future.successful(Left(NotificationSubmissionError.HttpError(response)))
        }
      }
  }

  def getStateOfWorkItem(idempotencyKey: String)(using HeaderCarrier): Future[NotificationState] = {
    protectedServiceConnector
      .getStateOfWorkItem(idempotencyKey)
      .flatMap { response =>
        response.status match {
          case NO_CONTENT =>
            Future.successful(Pending(idempotencyKey))
          case OK =>
            val notificationReference = Json.parse(response.body).as[NotificationResponse].notificationRef
            Future.successful(NotificationState.Success(notificationReference))
          case _ => Future.successful(NotificationState.Failure(response.status))
        }
      }
  }

  private def generateIdempotencyKey = UUID.randomUUID().toString
}

object NotificationSubmitService {

  enum NotificationState {
    case Success(subscriptionId: String) extends NotificationState
    case Pending(idempotencyKey: String) extends NotificationState
    case Failure(status: Int)            extends NotificationState
  }

  extension (userAnswers: UserAnswers) {
    def toNotification(idempotencyKey: Option[String]): NotificationRequest = {
      NotificationRequest(
        remarks = userAnswers.getNullable(NotificationAdditionalInformationPage),
        saos = userAnswers.toSaos,
        companies = userAnswers.toCompanies,
        idempotencyKey = idempotencyKey
      )
    }

    private def toSaos: List[Sao] = {
      @tailrec
      def previousSaos(mongoSaoIndex: Int = 0, saos: List[Sao] = Nil): List[Sao] = {
        userAnswers
          .get(NotificationMultiSaoPreviousOfficerNamePage(mongoSaoIndex, NormalMode)) match {
          case Some(name) =>
            previousSaos(
              mongoSaoIndex + 1,
              Sao(
                name = name,
                fromDate = userAnswers
                  .get(NotificationMultiSaoPreviousOfficerStartDatePage(mongoSaoIndex, NormalMode))
                  .map(_.toString),
                toDate = userAnswers
                  .get(NotificationMultiSaoPreviousOfficerEndDatePage(mongoSaoIndex, NormalMode))
                  .map(_.toString)
              ) :: saos
            )
          case None => saos.reverse
        }
      }

      userAnswers.get(NotificationMoreThanOneSaoPage(NormalMode)) match {
        case Some(true) =>
          Sao(
            name = userAnswers
              .get(NotificationMultiSaoLastOfficerNamePage(NormalMode))
              .fold(???)(identity),
            fromDate = userAnswers
              .get(NotificationMultiSaoLastOfficerStartDatePage(NormalMode))
              .map(_.toString),
            toDate = None
          ) :: previousSaos()
        case Some(false) =>
          List(
            Sao(
              name = userAnswers
                .get(NotificationSingleSaoOfficerNamePage(NormalMode))
                .fold(???)(identity),
              fromDate = None,
              toDate = None
            )
          )
        case None => ???
      }
    }

    private def toCompanies: List[Company] = {
      userAnswers
        .get(UploadTemplateTablePage)
        .fold(???)(data => data.rows.map(_.notification))
        .map(company =>
          Company(
            crn = company.companyCrn.map(_.value),
            utr = company.companyUtr.value,
            name = company.companyName,
            accPeriodEnd = company.financialYearEndDate.toString,
            status = company.companyStatus,
            `type` = company.companyType
          )
        )
        .toList
    }
  }
}
