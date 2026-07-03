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
import models.UserAnswers
import models.notification.*
import models.upload.UploadTemplateTableData
import pages.notification.*
import play.api.libs.json.Json
import repositories.SessionRepository
import services.NotificationSubmitService.*
import uk.gov.hmrc.http.HeaderCarrier

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class NotificationSubmitService @Inject() (
    protectedServiceConnector: ProtectedServiceConnector,
    sessionRepository: SessionRepository
)(using
    ec: ExecutionContext
) {
  def submit(userAnswers: UserAnswers)(using HeaderCarrier): Future[Either[NotificationSubmissionError, String]] = {
    protectedServiceConnector
      .postNotification(userAnswers.toNotification)
      .flatMap { response =>
        response.status match {
          case 201 => {
            val notificationReference = Json.parse(response.body).as[NotificationResponse].notificationRef
            sessionRepository
              .set(userAnswers.copy(data = Json.obj()))
              .map {
                case true => Right(notificationReference)
                case _    => Left(NotificationSubmissionError.MongoError)
              }
          }
          case _ => Future.successful(Left(NotificationSubmissionError.HttpError(response)))
        }
      }
  }
}

object NotificationSubmitService {

  val hardCodedSubscriptionId = "123" // TODO: remove when the subscription id is available

  extension (userAnswers: UserAnswers) {
    def toNotification: NotificationRequest = {
      NotificationRequest(
        subscriptionId = hardCodedSubscriptionId,
        remarks = userAnswers.getNullable(NotificationAdditionalInformationPage),
        saos = userAnswers.toSaos,
        companies = userAnswers.toCompanies
      )
    }

    private def toSaos: List[Sao] = {
      @tailrec
      def previousSaos(mongoSaoIndex: Int = 0, saos: List[Sao] = Nil): List[Sao] = {
        userAnswers
          .get(NotificationMultiSaoPreviousOfficerNamePage(mongoSaoIndex)) match {
          case Some(name) =>
            previousSaos(
              mongoSaoIndex + 1,
              Sao(
                name = name,
                fromDate = userAnswers
                  .get(NotificationMultiSaoPreviousOfficerStartDatePage(mongoSaoIndex))
                  .map(_.toString),
                email = None,
                toDate = userAnswers
                  .get(NotificationMultiSaoPreviousOfficerEndDatePage(mongoSaoIndex))
                  .map(_.toString)
              ) :: saos
            )
          case None => saos.reverse
        }
      }

      userAnswers.get(NotificationMoreThanOneSaoPage) match {
        case Some(true) =>
          Sao(
            name = userAnswers
              .get(NotificationMultiSaoLastOfficerNamePage)
              .fold(???)(identity),
            fromDate = userAnswers
              .get(NotificationMultiSaoLastOfficerStartDatePage)
              .map(_.toString),
            email = None,
            toDate = None
          ) :: previousSaos()
        case Some(false) =>
          List(
            Sao(
              name = userAnswers
                .get(NotificationSingleSaoOfficerNamePage)
                .fold(???)(identity),
              fromDate = None,
              email = None,
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
            crn = company.companyCrn.map(crn => crn.value),
            utr = company.companyUtr.value,
            name = company.companyName,
            accPeriodEnd = company.financialYearEndDate.toString,
            status = company.companyStatus.toString,
            `type` = company.companyType.toString
          )
        )
        .toList
    }
  }
}
