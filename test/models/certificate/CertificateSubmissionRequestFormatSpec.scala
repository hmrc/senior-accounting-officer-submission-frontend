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

package models.certificate

import base.SpecBase
import play.api.libs.json.*

class CertificateSubmissionRequestFormatSpec extends SpecBase {

  "CertificateSubmissionRequest" - {

    "must write to and read from JSON" in {
      Json.toJson(request) mustBe requestJson
      requestJson.validate[CertificateSubmissionRequest] mustBe JsSuccess(request)
    }

    "must write and read empty optional fields" in {
      val requestWithoutOptionalValues = request.copy(
        submitterName = None,
        companies = Seq(company.copy(crn = None)),
        remarks = None
      )

      val json = Json.toJson(requestWithoutOptionalValues)

      (json \ "submitterName").asOpt[String] mustBe None
      (json \ "companies" \ 0 \ "crn").asOpt[String] mustBe None
      (json \ "remarks").asOpt[String] mustBe None
      json.validate[CertificateSubmissionRequest] mustBe JsSuccess(requestWithoutOptionalValues)
    }

    "must fail to read when a required field is missing" in {
      val result = requestJson.as[JsObject].-("subscriptionId").validate[CertificateSubmissionRequest]

      result mustBe a[JsError]
    }
  }

  "CertificateSubmissionCompany" - {

    "must write to and read from JSON" in {
      Json.toJson(company) mustBe companyJson
      companyJson.validate[CertificateSubmissionCompany] mustBe JsSuccess(company)
    }

    "must fail to read when a tax regime flag is missing" in {
      val result = companyJson.as[JsObject].-("isVatQualified").validate[CertificateSubmissionCompany]

      result mustBe a[JsError]
    }
  }

  "CertificateSubmissionResponse" - {

    "must write to and read from JSON" in {
      val response = CertificateSubmissionResponse("CRT0123456789")
      val json     = Json.obj("certificateRef" -> "CRT0123456789")

      Json.toJson(response) mustBe json
      json.validate[CertificateSubmissionResponse] mustBe JsSuccess(response)
    }

    "must fail to read when certificateRef is missing" in {
      Json.obj().validate[CertificateSubmissionResponse] mustBe a[JsError]
    }
  }

  private val company: CertificateSubmissionCompany =
    CertificateSubmissionCompany(
      crn = Some("AB123456"),
      utr = "1234567890",
      name = "Example Ltd",
      accPeriodEnd = "2026-03-31",
      status = "COMPLIANT",
      `type` = "LTD",
      isCorporationTaxQualified = true,
      isVatQualified = false,
      isPayeQualified = false,
      isInsurancePremiumTaxQualified = false,
      isStampDutyLandTaxQualified = false,
      isStampDutyReserveTaxQualified = false,
      isPetroleumRevenueTaxQualified = false,
      isCustomsDutiesQualified = false,
      isExciseDutiesQualified = false,
      isBankLevyQualified = false
    )

  private val companyJson: JsValue = Json.parse(
    """{
      |  "crn": "AB123456",
      |  "utr": "1234567890",
      |  "name": "Example Ltd",
      |  "accPeriodEnd": "2026-03-31",
      |  "status": "COMPLIANT",
      |  "type": "LTD",
      |  "isCorporationTaxQualified": true,
      |  "isVatQualified": false,
      |  "isPayeQualified": false,
      |  "isInsurancePremiumTaxQualified": false,
      |  "isStampDutyLandTaxQualified": false,
      |  "isStampDutyReserveTaxQualified": false,
      |  "isPetroleumRevenueTaxQualified": false,
      |  "isCustomsDutiesQualified": false,
      |  "isExciseDutiesQualified": false,
      |  "isBankLevyQualified": false
      |}""".stripMargin
  )

  private val request: CertificateSubmissionRequest =
    CertificateSubmissionRequest(
      subscriptionId = testSaoSubscriptionId,
      submitterName = Some("Proxy Person"),
      saoName = "Senior Officer",
      saoEmail = "sao@example.com",
      companies = Seq(company),
      remarks = Some("Certificate remarks")
    )

  private val requestJson: JsValue = Json.parse(
    s"""{
      |  "subscriptionId": "$testSaoSubscriptionId",
      |  "submitterName": "Proxy Person",
      |  "saoName": "Senior Officer",
      |  "saoEmail": "sao@example.com",
      |  "companies": [
      |    {
      |      "crn": "AB123456",
      |      "utr": "1234567890",
      |      "name": "Example Ltd",
      |      "accPeriodEnd": "2026-03-31",
      |      "status": "COMPLIANT",
      |      "type": "LTD",
      |      "isCorporationTaxQualified": true,
      |      "isVatQualified": false,
      |      "isPayeQualified": false,
      |      "isInsurancePremiumTaxQualified": false,
      |      "isStampDutyLandTaxQualified": false,
      |      "isStampDutyReserveTaxQualified": false,
      |      "isPetroleumRevenueTaxQualified": false,
      |      "isCustomsDutiesQualified": false,
      |      "isExciseDutiesQualified": false,
      |      "isBankLevyQualified": false
      |    }
      |  ],
      |  "remarks": "Certificate remarks"
      |}""".stripMargin
  )
}
