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

package models.notification

import models.UserAnswers
import repositories.SessionRepository
import javax.inject.Inject
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.HeaderCarrier
import play.api.libs.json.OFormat
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import pages.notification.NotificationAdditionalInformationPage
import play.api.libs.json.Reads
import connectors.ProtectedServiceConnector
import pages.notification.NotificationMoreThanOneSaoPage
import pages.notification.NotificationSingleSaoOfficerNamePage
import pages.notification.NotificationMultiSaoLastOfficerNamePage
import scala.annotation.tailrec
import pages.notification.NotificationMultiSaoPreviousOfficerNamePage
import pages.notification.NotificationMultiSaoLastOfficerStartDatePage
import pages.notification.NotificationMultiSaoPreviousOfficerStartDatePage
import pages.notification.NotificationMultiSaoPreviousOfficerEndDatePage
import pages.notification.UploadTemplateTablePage

final case class NotificationRequest(
    subscriptionId: String,
    companies: List[Company],
    saos: List[Sao],
    remarks: Option[String]
)

object NotificationRequest {
  given format: OFormat[NotificationRequest] = Json.format
}
