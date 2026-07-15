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
import models.UserAnswers
import models.certificate.*
import models.upload.{CompanyStatus, ParsedSubmissionRow}
import pages.certificate.*
import play.api.Logging
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CertificateSubmissionService @Inject() (
    connector: CertificateSubmissionConnector,
    sessionRepository: SessionRepository
)(using ExecutionContext)
    extends Logging {

  import CertificateSubmissionService.*

  def submit(
      userId: String,
      saoSubscriptionId: String,
      userAnswers: UserAnswers,
      token: String
  )(using HeaderCarrier): Future[CertificateSubmissionResult] =
    buildRequest(saoSubscriptionId, userAnswers) match {
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
              .flatMap(response =>
                sessionRepository.clear(userId).map(_ => CertificateSubmissionResult.Submitted(response.certificateRef))
              )
              .recover { case e =>
                logger.error("Certificate submission failed", e)
                CertificateSubmissionResult.Failed
              }
        }
    }

  private def buildRequest(
      saoSubscriptionId: String,
      userAnswers: UserAnswers
  ): Either[String, CertificateSubmissionRequest] =
    for {
      saoName   <- userAnswers.get(CertificateSaoFullNamePage).toRight("missing SAO name")
      saoEmail  <- userAnswers.get(CertificateSaoEmailPage).toRight("missing SAO email")
      tableData <- userAnswers.get(CertificateUploadTemplateTablePage).toRight("missing uploaded certificate data")
      companies = tableData.rows.map(toCompany)
      _ <- Either.cond(companies.nonEmpty, (), "missing companies")
    } yield CertificateSubmissionRequest(
      subscriptionId = saoSubscriptionId,
      submitterName = submitterName(userAnswers),
      SAOName = saoName,
      SAOEmail = saoEmail,
      companies = companies,
      remarks = userAnswers.getNullable(CertificateAdditionalInformationPage)
    )

  private def submitterName(userAnswers: UserAnswers): Option[String] =
    userAnswers
      .get(CertificateWhoIsSubmittingPage)
      .collect { case CertificateWhoIsSubmitting.StandIn =>
        userAnswers.get(CertificateDeclarationStandInPage).map(_.StandInName)
      }
      .flatten

  private def toCompany(row: ParsedSubmissionRow): CertificateSubmissionCompany =
    CertificateSubmissionCompany(
      crn = row.notification.companyCrn.map(_.value),
      utr = row.notification.companyUtr.value,
      name = row.notification.companyName,
      accPeriodEnd = row.notification.financialYearEndDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
      status = status(row.notification.companyStatus),
      `type` = row.notification.companyType.toString,
      isCorporationTaxQualified = row.certificate.corporationTax,
      isVatQualified = row.certificate.valueAddedTax,
      isPayeQualified = row.certificate.paye,
      isInsurancePremiumTaxQualified = row.certificate.insurancePremiumTax,
      isStampDutyLandTaxQualified = row.certificate.stampDutyLandTax,
      isStampDutyReserveTaxQualified = row.certificate.stampDutyReserveTax,
      isPetroleumRevenueTaxQualified = row.certificate.petroleumRevenueTax,
      isCustomsDutiesQualified = row.certificate.customsDuties,
      isExciseDutiesQualified = row.certificate.exciseDuties,
      isBankLevyQualified = row.certificate.bankLevy
    )

  private def status(companyStatus: CompanyStatus): String =
    companyStatus match {
      case CompanyStatus.Active         => "COMPLIANT"
      case CompanyStatus.Dormant        => "DORMANT"
      case CompanyStatus.Administration => "ADMINISTRATION"
      case CompanyStatus.Liquidation    => "LIQUIDATION"
    }
}

object CertificateSubmissionService {
  enum CertificateSubmissionResult {
    case Submitted(certificateRef: String)
    case MissingData
    case Duplicate
    case Failed
  }
}
