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
import javax.inject.Inject
import models.UserAnswers
import models.notification.*
import pages.notification.*
import play.api.libs.json.{Json, Reads}
import repositories.SessionRepository
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

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

val hardCodedSubscriptionId = "123" // TODO: remove when the subscription id is available

// TODO: where should the extensions live?
extension (userAnswers: UserAnswers) {

  def toNotification: NotificationRequest = {
    NotificationRequest(
      subscriptionId = hardCodedSubscriptionId,
      remarks = userAnswers.getNullable(NotificationAdditionalInformationPage),
      saos = toSaos,
      companies = toCompanies
    )
  }

  def toSaos: List[Sao] = {
    @tailrec
    def previousSaos(index: Int = 0, saos: List[Sao] = List()): List[Sao] = {
      userAnswers
        .get(NotificationMultiSaoPreviousOfficerNamePage(index)) match {
        case Some(name) =>
          previousSaos(
            index + 1,
            Sao(
              name = name,
              fromDate = userAnswers
                .get(NotificationMultiSaoPreviousOfficerStartDatePage(index))
                .map(_.toString()),
              email = None,
              toDate = userAnswers
                .get(NotificationMultiSaoPreviousOfficerEndDatePage(index))
                .map(_.toString())
            ) :: saos
          )
        case None => saos
      }
    }

    userAnswers.get(NotificationMoreThanOneSaoPage) match {
      case Some(true) =>
        Sao(
          name = userAnswers
            .get(NotificationMultiSaoLastOfficerNamePage)
            .fold(???)(name => name),
          fromDate = userAnswers
            .get(NotificationMultiSaoLastOfficerStartDatePage)
            .map(_.toString()),
          email = None,
          toDate = None
        ) :: previousSaos()
      case Some(false) =>
        List(
          Sao(
            name = userAnswers
              .get(NotificationSingleSaoOfficerNamePage)
              .fold(???)(name => name),
            fromDate = None,
            email = None,
            toDate = None
          )
        )
      case None => ???
    }
  }

  def toCompanies: List[Company] = {
    val notificationCompany =
      userAnswers
        .get(UploadTemplateTablePage)
        .fold(???)(data => data.rows.map(_.notification))
    notificationCompany
      .map(company =>
        Company(
          crn = company.companyCrn.map(crn => crn.value),
          utr = company.companyUtr.value,
          name = company.companyName,
          accPeriodEnd = company.financialYearEndDate.toString(),
          status = company.companyStatus.toString(),
          `type` = company.companyType.toString()
        )
      )
      .toList
  }
}
