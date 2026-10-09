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

import connectors.CertificateSubmissionConnector
import models.NormalMode
import models.UserAnswers
import models.certificate.*
import models.upload.{CertificateFields, NotificationFields, ParsedSubmissionRow}
import pages.certificate.*
import play.api.Logging
import play.api.http.Status.{CREATED, NO_CONTENT, OK}
import play.api.libs.json.{JsSuccess, Json}
import repositories.SessionRepository
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}

import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class CertificateSubmissionService @Inject() (
    connector: CertificateSubmissionConnector,
    sessionRepository: SessionRepository
)(using ExecutionContext)
    extends Logging {

  import CertificateSubmissionService.*

  def submit(
      userId: String,
      userAnswers: UserAnswers,
      token: String
  )(using HeaderCarrier): Future[CertificateSubmissionResult] =
    buildRequest(userAnswers, None) match {
      case Left(error) =>
        logger.warn(s"Certificate submission could not be built: $error")
        Future.successful(CertificateSubmissionResult.MissingData)
      case Right(request) =>
        sessionRepository.claimCertificateSubmissionToken(userId, token).flatMap {
          case false =>
            logger.warn("Certificate submission token was missing or already used")
            Future.successful(CertificateSubmissionResult.Duplicate)
          case true =>
            connector
              .submit(request)
              .flatMap { response =>
                sessionRepository
                  .set(userAnswers.copy(data = Json.obj()))
                  .recover { case e =>
                    logger.warn(s"Certificate ${response.certificateRef} submitted but wiping journey data failed", e)
                    true
                  }
                  .map(_ => CertificateSubmissionResult.Submitted(response.certificateRef))
              }
              .recover { case e =>
                logger.error("Certificate submission failed", e)
                CertificateSubmissionResult.Failed
              }
        }
    }

  def submitWithFaultTolerance(
      userId: String,
      userAnswers: UserAnswers,
      token: String
  )(using HeaderCarrier): Future[CertificateSubmissionResult] =
    buildRequest(userAnswers, Some(generateIdempotencyKey)) match {
      case Left(error) =>
        logger.warn(s"Certificate submission could not be built: $error")
        Future.successful(CertificateSubmissionResult.MissingData)
      case Right(request) =>
        sessionRepository.claimCertificateSubmissionToken(userId, token).flatMap {
          case false =>
            logger.warn("Certificate submission token was missing or already used")
            Future.successful(CertificateSubmissionResult.Duplicate)
          case true =>
            connector
              .submitWithFaultTolerance(request)
              .flatMap { response =>
                sessionRepository
                  .set(userAnswers.copy(data = Json.obj()))
                  .recover { case e =>
                    logger.warn(s"Certificate submitted but wiping journey data failed", e)
                    true
                  }
                  .map(_ => CertificateSubmissionResult.Pending(response.idempotencyKey))
              }
              .recover { case e =>
                logger.error("Certificate submission failed", e)
                CertificateSubmissionResult.Failed
              }
        }
    }

  def getStateOfWorkItem(
      idempotencyKey: String
  )(using HeaderCarrier): Future[CertificateSubmissionResult] =
    connector
      .getStateOfWorkItem(idempotencyKey)
      .map {
        case response if response.status == OK =>
          response.json.validate[CertificateSubmissionResponse] match {
            case JsSuccess(value, _) => CertificateSubmissionResult.Submitted(value.certificateRef)
            case _                   =>
              throw UpstreamErrorResponse(
                "Certificate submission response did not contain a valid certificateRef",
                CREATED
              )
          }
        case response if response.status == NO_CONTENT => CertificateSubmissionResult.Pending(idempotencyKey)
        case response                                  => throw UpstreamErrorResponse(response.body, response.status)
      }
      .recover { case e =>
        logger.error("Certificate submission failed", e)
        CertificateSubmissionResult.Failed
      }

  private def buildRequest(
      userAnswers: UserAnswers,
      idempotencyKey: Option[String]
  ): Either[String, CertificateSubmissionRequest] =
    for {
      saoName            <- userAnswers.get(CertificateSaoFullNamePage).toRight("missing SAO name")
      saoEmail           <- userAnswers.get(CertificateSaoEmailPage).toRight("missing SAO email")
      saoDeclarationName <- declarationName(userAnswers).toRight("missing SAO declaration name")
      tableData <- userAnswers.get(CertificateUploadTemplateTablePage).toRight("missing uploaded certificate data")
      companies = tableData.rows.collect { case ParsedSubmissionRow(notification, Some(certificate)) =>
        toCompany(notification, certificate)
      }
      _ <- Either.cond(companies.nonEmpty, (), "missing companies")
    } yield CertificateSubmissionRequest(
      submitterName = submitterName(userAnswers),
      saoName = saoName,
      saoDeclarationName = saoDeclarationName,
      saoEmail = saoEmail,
      companies = companies,
      remarks = userAnswers.getNullable(CertificateAdditionalInformationPage),
      idempotencyKey = idempotencyKey
    )

  private def declarationName(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(CertificateWhoIsSubmittingPage(NormalMode)).flatMap {
      case CertificateWhoIsSubmitting.Sao =>
        userAnswers.get(CertificateDeclarationSaoPage(NormalMode))
      case CertificateWhoIsSubmitting.StandIn =>
        userAnswers.get(CertificateDeclarationStandInPage(NormalMode)).map(_.SaoName)
    }

  private def submitterName(userAnswers: UserAnswers): Option[String] =
    userAnswers
      .get(CertificateWhoIsSubmittingPage(NormalMode))
      .collect { case CertificateWhoIsSubmitting.StandIn =>
        userAnswers.get(CertificateDeclarationStandInPage(NormalMode)).map(_.StandInName)
      }
      .flatten

  private def toCompany(
      notification: NotificationFields,
      certificate: CertificateFields
  ): CertificateSubmissionCompany =
    CertificateSubmissionCompany(
      crn = notification.companyCrn.map(_.value),
      utr = notification.companyUtr.value,
      name = notification.companyName,
      accPeriodEnd = notification.financialYearEndDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
      status = notification.companyStatus,
      `type` = notification.companyType,
      isCorporationTaxQualified = certificate.corporationTax,
      isVatQualified = certificate.valueAddedTax,
      isPayeQualified = certificate.paye,
      isInsurancePremiumTaxQualified = certificate.insurancePremiumTax,
      isStampDutyLandTaxQualified = certificate.stampDutyLandTax,
      isStampDutyReserveTaxQualified = certificate.stampDutyReserveTax,
      isPetroleumRevenueTaxQualified = certificate.petroleumRevenueTax,
      isCustomsDutiesQualified = certificate.customsDuties,
      isExciseDutiesQualified = certificate.exciseDuties,
      isBankLevyQualified = certificate.bankLevy,
      qualificationStatement = certificate.qualificationStatement
    )

  private def generateIdempotencyKey = UUID.randomUUID().toString
}

object CertificateSubmissionService {
  enum CertificateSubmissionResult {
    case Submitted(certificateRef: String)
    case Pending(idempotencyKey: String)
    case MissingData
    case Duplicate
    case Failed
  }
}
