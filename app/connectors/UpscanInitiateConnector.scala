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

package connectors

import config.AppConfig
import controllers.certificate.routes as certificateRoutes
import controllers.notification.routes as notificationRoutes
import controllers.combined.uploadTemplate.routes as uploadTemplateRoutes
import models.upscan.{UploadJourney, UpscanInitiateRequestV2, UpscanInitiateResponse}
import play.api.libs.json.*
import play.api.libs.ws.writeableOf_JsValue
import play.api.mvc.Call
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class UpscanInitiateConnector @Inject() (
    httpClient: HttpClientV2,
    appConfig: AppConfig
)(using ExecutionContext) {

  def initiateV2(journey: UploadJourney, isCombinedJourney: Boolean)(using HeaderCarrier): Future[UpscanInitiateResponse] = {
    val request = UpscanInitiateRequestV2(
      callbackUrl = appConfig.upscanCallbackTarget(journey),
      successRedirect = Some(appConfig.host + successRedirect(journey, isCombinedJourney)),
      errorRedirect = Some(appConfig.host + errorRedirect(journey, isCombinedJourney)),
      maximumFileSize = Some(appConfig.maxUploadFileSizeBytes)
    )

    httpClient
      .post(url"${appConfig.upscanInitiateV2Url}")
      .withBody(Json.toJson(request))
      .execute[UpscanInitiateResponse]
  }

  private def successRedirect(journey: UploadJourney, isCombinedJourney: Boolean): Call =
    (journey, isCombinedJourney) match {
      case (UploadJourney.Notification, false) => notificationRoutes.NotificationUploadSuccessController.onPageLoad(key = None)
      case (UploadJourney.Notification, true)  => uploadTemplateRoutes.CombinedUploadSuccessController.onPageLoad(key = None)
      case (UploadJourney.Certificate, _)      => certificateRoutes.CertificateUploadSuccessController.onPageLoad(key = None)
    }

  private def errorRedirect(journey: UploadJourney, isCombinedJourney: Boolean): Call =
    (journey, isCombinedJourney) match {
      case (UploadJourney.Notification, false) => notificationRoutes.NotificationUploadFormController.onPageLoad()
      case (UploadJourney.Notification, true)  => uploadTemplateRoutes.CombinedUploadFormController.onPageLoad()
      case (UploadJourney.Certificate, _)      => certificateRoutes.CertificateUploadFormController.onPageLoad()
    }
}
