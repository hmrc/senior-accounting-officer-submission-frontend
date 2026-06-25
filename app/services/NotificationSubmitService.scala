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

import models.UserAnswers
import repositories.SessionRepository
import javax.inject.Inject
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import java.net.URL
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.HeaderCarrier
import play.api.libs.json.OFormat
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import scala.concurrent.ExecutionContext
import uk.gov.hmrc.http.HttpResponse
import scala.concurrent.Future
import pages.notification.NotificationAdditionalInformationPage
import play.api.libs.json.Reads

class NotificationSubmitService @Inject() (httpClient: HttpClientV2, sessionRepository: SessionRepository)(using
    ec: ExecutionContext
) {
  def submit(userAnswers: UserAnswers)(using hc: HeaderCarrier): Future[Either[NotificationSubmissionError, String]] = {
    // TODO: get the url properly
    httpClient
      .post(URL("http://localhost:10060/notification"))
      .withBody(Json.toJson(userAnswers.toNotification))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case 201 => {
            sessionRepository
              .set(userAnswers.copy(data = Json.obj()))
              .map {
                case true =>
                  Right(Json.toJson(response.body).as[NotificationResponse].notificationReference)
                case _ => Left(NotificationSubmissionError.MongoError)
              }
          }
          case _ => Future.successful(Left(NotificationSubmissionError.HttpError))
        }
      }
  }
}

enum NotificationSubmissionError(val message: String) {
  case MongoError extends NotificationSubmissionError("Problem with mongo")
  case HttpError  extends NotificationSubmissionError("Problem with http client")
}

final case class NotificationRequest(
    subscriptionId: String,
    companies: List[Company],
    saos: List[Sao],
    remarks: Option[String]
)

object NotificationRequest {
  given format: OFormat[NotificationRequest] = Json.format
}

final case class Company(
    crn: Option[String] = None,
    utr: String,
    name: String,
    accPeriodEnd: String,
    status: String,
    `type`: String
)

object Company {
  given OFormat[Company] = Json.format[Company]
}

final case class Sao(
    name: String,
    fromDate: Option[String],
    email: Option[String],
    toDate: Option[String]
)

object Sao {
  given OFormat[Sao] = Json.format[Sao]
}

extension (userAnswers: UserAnswers) {
  def toNotification: NotificationRequest = {
    NotificationRequest(
      subscriptionId = "TODO where do i come from?", // TODO: figure out where subscription id comes from
      remarks = userAnswers.getNullable(NotificationAdditionalInformationPage),
      saos = List(),     // TODO: map saos
      companies = List() // TODO: map companies
    )
  }
}

final case class NotificationResponse(notificationReference: String)

object NotificationResponse {
  given format: OFormat[NotificationResponse] = Json.format
}
