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

import base.SpecBase
import play.api.libs.json.{JsError, JsString, Json}

import java.time.LocalDate

class ParsedSubmissionRowSpec extends SpecBase {
  import NotificationFields.given

  "ParsedSubmissionRow json formats" - {

    "must round-trip a fully-populated parsed submission row" in {
      val row = ParsedSubmissionRow(
        notification = NotificationFields(
          companyName = "Acme Ltd",
          companyUtr = CompanyUtr("0123456789"),
          companyCrn = Some(CompanyCrn("A1B2C3D4")),
          companyType = CompanyType.LTD,
          companyStatus = CompanyStatus.Active,
          financialYearEndDate = LocalDate.of(2025, 12, 31)
        ),
        certificate = CertificateFields(
          corporationTax = true,
          valueAddedTax = false,
          paye = true,
          insurancePremiumTax = false,
          stampDutyLandTax = false,
          stampDutyReserveTax = false,
          petroleumRevenueTax = false,
          customsDuties = false,
          exciseDuties = false,
          bankLevy = false,
          certificateType = Some(CertificateType.Qualified),
          additionalInformation = Some("Additional context")
        )
      )

      Json.toJson(row).validate[ParsedSubmissionRow].get mustBe row
    }

    "must read local date values in dd/MM/yyyy format" in {
      JsString("31/12/2025").validate[LocalDate].get mustBe LocalDate.of(2025, 12, 31)
    }

    "must reject invalid local date values" in {
      JsString("31/02/2025").validate[LocalDate] mustBe JsError("Invalid date value: 31/02/2025")
    }
  }

  "value parsers" - {

    "must parse and trim CompanyUtr and CompanyCrn values" in {
      CompanyUtr.fromString(" 0123456789 ").value mustBe CompanyUtr("0123456789")
      CompanyCrn.fromString(" A1B2C3D4 ").value mustBe CompanyCrn("A1B2C3D4")
    }

    "must reject invalid CompanyUtr and CompanyCrn values" in {
      CompanyUtr.fromString("123") mustBe None
      CompanyCrn.fromString("12") mustBe None
    }

    "must parse enum values case-insensitively" in {
      CompanyType.fromString("plc").value mustBe CompanyType.PLC
      CompanyStatus.fromString("dormant").value mustBe CompanyStatus.Dormant
      CertificateType.fromString("unQUALified").value mustBe CertificateType.Unqualified
    }

    "must reject unknown enum values" in {
      JsString("OTHER").validate[CompanyType] mustBe JsError("Unknown enum value: OTHER")
      JsString("OTHER").validate[CompanyStatus] mustBe JsError("Unknown enum value: OTHER")
      JsString("OTHER").validate[CertificateType] mustBe JsError("Unknown enum value: OTHER")
    }
  }
}
