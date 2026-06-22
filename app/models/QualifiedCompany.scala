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

package models

import play.api.i18n.Messages

final case class QualifiedCompany(
    name: String,
    utr: String,
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
    additionalInformation: String
)

extension (qualifiedCompany: QualifiedCompany) {
  def displayRegimes(using messages: Messages): String = {
    List(
      qualifiedCompany.corporationTax      -> "certificateReviewQualified.taxRegimes.corporationTax",
      qualifiedCompany.valueAddedTax       -> "certificateReviewQualified.taxRegimes.valueAddedTax",
      qualifiedCompany.paye                -> "certificateReviewQualified.taxRegimes.paye",
      qualifiedCompany.insurancePremiumTax -> "certificateReviewQualified.taxRegimes.insurancePremiumTax",
      qualifiedCompany.stampDutyLandTax    -> "certificateReviewQualified.taxRegimes.stampDutyLandTax",
      qualifiedCompany.stampDutyReserveTax -> "certificateReviewQualified.taxRegimes.stampDutyReserveTax",
      qualifiedCompany.petroleumRevenueTax -> "certificateReviewQualified.taxRegimes.petroleumRevenueTax",
      qualifiedCompany.customsDuties       -> "certificateReviewQualified.taxRegimes.customsDuties",
      qualifiedCompany.exciseDuties        -> "certificateReviewQualified.taxRegimes.exciseDuties",
      qualifiedCompany.bankLevy            -> "certificateReviewQualified.taxRegimes.bankLevy"
    ).collect { case (true, messageKey) =>
      messages(messageKey)
    }.mkString(", ")
  }
}
