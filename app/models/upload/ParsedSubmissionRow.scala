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

package models.upload

import play.api.libs.json.{OFormat, Json}

final case class ParsedSubmissionRow(
    notification: NotificationFields,
    certificate: CertificateFields
)

object ParsedSubmissionRow {
  given OFormat[ParsedSubmissionRow] = Json.format[ParsedSubmissionRow]
}

final case class NotificationFields(
    companyName: String,
    companyUtr: String,
    companyCrn: String,
    companyType: String,
    companyStatus: String,
    financialYearEndDate: String
)

object NotificationFields {
  given OFormat[NotificationFields] = Json.format[NotificationFields]
}

final case class CertificateFields(
    corporationTax: Boolean,
    valueAddedTax: Boolean,
    paye: Boolean,
    insurancePremiumTax: Boolean,
    stampDutyLandTax: Boolean,
    stampDutyReserveTax: Boolean,
    petroleumRevenueTax: Boolean,
    customsDuties: Boolean,
    exciseDuties: Boolean,
    bankLevy: Boolean,
    certificateType: Option[String],
    additionalInformation: Option[String]
)

object CertificateFields {
  given OFormat[CertificateFields] = Json.format[CertificateFields]
}
