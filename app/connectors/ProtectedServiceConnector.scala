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

import uk.gov.hmrc.http.client.HttpClientV2
import config.AppConfig
import javax.inject.Inject
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.HeaderCarrier
import play.api.libs.json.Json
import models.notification.NotificationRequest
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import scala.concurrent.ExecutionContext
import uk.gov.hmrc.http.HttpReads.Implicits.*
import scala.concurrent.Future
import uk.gov.hmrc.http.StringContextOps

class ProtectedServiceConnector @Inject() (appConfig: AppConfig, httpClient: HttpClientV2)(using
    ec: ExecutionContext
) {
  def postNotification(request: NotificationRequest)(using hc: HeaderCarrier): Future[HttpResponse] = {
    httpClient
      .post(url"${appConfig.protectedServiceUrl}/senior-accounting-officer/notification")
      .withBody(Json.toJson(request))
      .execute[HttpResponse]
  }
}
