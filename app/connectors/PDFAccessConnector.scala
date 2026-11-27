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

package connectors

import config.AppConfig
import uk.gov.hmrc.http.client.{HttpClientV2, readStreamHttpResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

import java.util.UUID
import javax.inject.Inject

class PDFAccessConnector @Inject() (appConfig: AppConfig, httpClient: HttpClientV2)(using ec: ExecutionContext) {
  def headNotificationPDFStatus(saoSubscriptionId: String, notificationReference: String)(using
      hc: HeaderCarrier
  ): Future[HttpResponse] = {
    httpClient
      .head(
        url"${appConfig.protectedServiceUrl}/senior-accounting-officer/notificationPdf/$saoSubscriptionId/$notificationReference"
      )
      .setHeader("correlationId" -> UUID.randomUUID().toString)
      .execute[HttpResponse]
  }

  def headCertificatePDFStatus(saoSubscriptionId: String, certificateReference: String)(using
      hc: HeaderCarrier
  ): Future[HttpResponse] = {
    httpClient
      .head(
        url"${appConfig.protectedServiceUrl}/senior-accounting-officer/certificatePdf/$saoSubscriptionId/$certificateReference"
      )
      .setHeader("correlationId" -> UUID.randomUUID().toString)
      .execute[HttpResponse]
  }

  def getCertificatePDF(saoSubscriptionId: String, certificateReference: String)(using
      hc: HeaderCarrier
  ): Future[HttpResponse] = {
    httpClient
      .get(
        url"${appConfig.protectedServiceUrl}/senior-accounting-officer/certificatePdf/$saoSubscriptionId/$certificateReference"
      )
      .setHeader("correlationId" -> UUID.randomUUID().toString)
      .stream[HttpResponse]
  }

  def getNotificationPDF(saoSubscriptionId: String, notificationReference: String)(using
      hc: HeaderCarrier
  ): Future[HttpResponse] = {
    httpClient
      .get(
        url"${appConfig.protectedServiceUrl}/senior-accounting-officer/notificationPdf/$saoSubscriptionId/$notificationReference"
      )
      .setHeader("correlationId" -> UUID.randomUUID().toString)
      .stream[HttpResponse]
  }

  def getDocumentumPackage(submissionId: String, fileName: String)(using hc: HeaderCarrier): Future[HttpResponse] = {
    httpClient
      .get(url"${appConfig.protectedServiceUrl}/senior-accounting-officer/documentumPackage/$submissionId/$fileName")
      .setHeader("correlationId" -> UUID.randomUUID().toString)
      .stream[HttpResponse]
  }
}
