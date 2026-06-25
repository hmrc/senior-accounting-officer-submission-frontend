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
import controllers.notification.routes as notificationRoutes
import controllers.routes
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

  def initiateV2(journey: UploadJourney)(using HeaderCarrier): Future[UpscanInitiateResponse] = {
    val request = UpscanInitiateRequestV2(
      callbackUrl = appConfig.upscanCallbackTarget(journey),
      successRedirect = Some(appConfig.host + successRedirect(journey)),
      errorRedirect = Some(appConfig.host + errorRedirect(journey))
    )

    httpClient
      .post(url"${appConfig.upscanInitiateV2Url}")
      .withBody(Json.toJson(request))
      .execute[UpscanInitiateResponse]
  }

  private def successRedirect(journey: UploadJourney): Call =
    journey match {
      case UploadJourney.Notification => notificationRoutes.NotificationUploadSuccessController.onPageLoad(key = None)
      case UploadJourney.Certificate  => routes.CertificateUploadFormController.onPageLoad()
    }

  private def errorRedirect(journey: UploadJourney): Call =
    journey match {
      case UploadJourney.Notification => notificationRoutes.NotificationUploadFormController.onPageLoad()
      case UploadJourney.Certificate  => routes.CertificateUploadFormController.onPageLoad()
    }
}
