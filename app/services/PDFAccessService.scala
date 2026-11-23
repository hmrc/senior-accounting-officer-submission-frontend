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

import connectors.PDFAccessConnector
import models.SubmissionDocumentType
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.control.NonFatal

import javax.inject.Inject

final case class DownloadedPDF(
    content: Source[ByteString, ?],
    contentLength: Option[Long],
    contentType: String
)

class PDFAccessService @Inject (
    pdfAccessConnector: PDFAccessConnector
)(using
    ec: ExecutionContext
) {
  def checkPDFStatus(saoSubscriptionId: String, reference: String, documentType: SubmissionDocumentType)(using
      HeaderCarrier
  ): Future[Boolean] = {
    val availabilityCheck = documentType match {
      case models.SubmissionDocumentType.Notification =>
        pdfAccessConnector
          .headNotificationPDFStatus(saoSubscriptionId = saoSubscriptionId, notificationReference = reference)
      case models.SubmissionDocumentType.Certificate =>
        pdfAccessConnector
          .headCertificatePDFStatus(saoSubscriptionId = saoSubscriptionId, certificateReference = reference)
    }
    availabilityCheck
      .map { response =>
        response.status match {
          case OK        => true
          case NOT_FOUND => false
          case _         => false
        }
      }
      .recover { case NonFatal(_) => false }
  }

  def getPDF(saoSubscriptionId: String, reference: String, documentType: SubmissionDocumentType)(using
      HeaderCarrier
  ): Future[Option[DownloadedPDF]] = {

    val response = documentType match {
      case SubmissionDocumentType.Notification =>
        pdfAccessConnector.getNotificationPDF(
          saoSubscriptionId = saoSubscriptionId,
          notificationReference = reference
        )

      case SubmissionDocumentType.Certificate =>
        pdfAccessConnector.getCertificatePDF(
          saoSubscriptionId = saoSubscriptionId,
          certificateReference = reference
        )
    }

    response.flatMap { response =>
      response.status match {
        case OK =>
          Future.successful(
            Some(
              DownloadedPDF(
                content = response.bodyAsSource,
                contentLength = response.header("Content-Length").map(_.toLong),
                contentType = response.header("Content-Type").getOrElse("application/pdf")
              )
            )
          )

        case NOT_FOUND =>
          Future.successful(None)

        case _ =>
          Future.failed(
            new RuntimeException(s"Unexpected PDF response status: ${response.status}")
          )
      }
    }
  }

  def downloadDocumentumPackage(submissionId: String, fileName: String)(using
      HeaderCarrier
  ): Future[Option[Source[ByteString, ?]]] = {
    pdfAccessConnector
      .getDocumentumPackage(submissionId, fileName)
      .flatMap { response =>
        response.status match {
          case OK        => Future.successful(Some(response.bodyAsSource))
          case NOT_FOUND => Future.successful(None)
          case _         => Future.failed(new RuntimeException(s"Unexpected PDF response status: ${response.status}"))
        }
      }
  }
}
