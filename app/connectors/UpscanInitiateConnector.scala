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
import connectors.UpscanInitiateConnector.UpscanJourney
import controllers.notification.routes as notificationRoutes
import models.upscan.{UpscanInitiateRequestV2, UpscanInitiateResponse}
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

  def initiateV2(journey: UpscanJourney)(using HeaderCarrier): Future[UpscanInitiateResponse] = {
    val request = UpscanInitiateRequestV2(
      callbackUrl = appConfig.upscanCallbackTarget,
      successRedirect = Some(appConfig.host + journey.successRedirect),
      errorRedirect = Some(appConfig.host + journey.errorRedirect)
    )

    httpClient
      .post(url"${appConfig.upscanInitiateV2Url}")
      .withBody(Json.toJson(request))
      .execute[UpscanInitiateResponse]
  }

}

object UpscanInitiateConnector {
  enum UpscanJourney(val successRedirect: Call, val errorRedirect: Call) {
    case Notification
        extends UpscanJourney(
          successRedirect = notificationRoutes.NotificationUploadSuccessController.onPageLoad(key = None),
          errorRedirect = notificationRoutes.NotificationUploadFormController.onPageLoad()
        )
  }
}
