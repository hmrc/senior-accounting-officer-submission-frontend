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

import base.SpecBase
import models.upload.{CompanyStatus, CompanyType}
import play.api.libs.json.*

class NotificationRequestSpec extends SpecBase {

  private val company = Company(
    crn = Some("1234567890"),
    utr = "0123456789",
    name = "ABCD",
    accPeriodEnd = "20/10/2025",
    status = CompanyStatus.Active,
    `type` = CompanyType.LTD
  )

  private val sao = Sao(
    name = "ABCDE",
    fromDate = Some("12/10/2025"),
    toDate = Some("12/11/2025")
  )

  private val request = NotificationRequest(
    companies = List(company),
    saos = List(sao),
    remarks = Some("no remarks"),
    idempotencyKey = Some("Some-idempotency-key")
  )

  private val companyJson = Json.toJson(company)
  private val saoJson     = Json.toJson(sao)
  private val requestJson = Json.toJson(request)

  "NotificationRequest" - {
    "must write to and read from JSON" in {
      requestJson.validate[NotificationRequest] mustBe JsSuccess(request)
    }

    "must write and read when optional fields are empty" in {
      val requestWithoutOptionalValues = request.copy(
        remarks = None,
        idempotencyKey = None
      )
      val json = Json.toJson(requestWithoutOptionalValues)

      (json \ "remarks").asOpt[String] mustBe None
      (json \ "idempotencyKey").asOpt[String] mustBe None
      json.validate[NotificationRequest] mustBe JsSuccess(requestWithoutOptionalValues)
    }

    Seq("companies", "saos").foreach { fieldName =>
      s"must fail to read when required field '$fieldName' is missing" in {
        (requestJson.as[JsObject] - fieldName).validate[NotificationRequest] mustBe a[JsError]
      }
    }
  }

  "Company" - {
    "must write to and read from JSON" in {
      companyJson.validate[Company] mustBe JsSuccess(company)
    }

    "must write and read when the optional CRN is empty" in {
      val companyWithoutCrn = company.copy(crn = None)
      val json              = Json.toJson(companyWithoutCrn)

      (json \ "crn").asOpt[String] mustBe None
      json.validate[Company] mustBe JsSuccess(companyWithoutCrn)
    }

    Seq("utr", "name", "accPeriodEnd", "status", "type").foreach { fieldName =>
      s"must fail to read when required field '$fieldName' is missing" in {
        (companyJson.as[JsObject] - fieldName).validate[Company] mustBe a[JsError]
      }
    }
  }

  "Sao" - {
    "must write to and read from JSON" in {
      saoJson.validate[Sao] mustBe JsSuccess(sao)
    }

    "must write and read when optional dates are empty" in {
      val saoWithoutDates = sao.copy(fromDate = None, toDate = None)
      val json            = Json.toJson(saoWithoutDates)

      (json \ "fromDate").asOpt[String] mustBe None
      (json \ "toDate").asOpt[String] mustBe None
      json.validate[Sao] mustBe JsSuccess(saoWithoutDates)
    }

    "must fail to read when the required name is missing" in {
      (saoJson.as[JsObject] - "name").validate[Sao] mustBe a[JsError]
    }
  }
}
