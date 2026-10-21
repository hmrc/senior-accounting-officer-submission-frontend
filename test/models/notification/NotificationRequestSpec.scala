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
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.*

import NotificationRequestGeneratorHelper.*

class NotificationRequestSpec extends SpecBase with ScalaCheckPropertyChecks {

  "NotificationRequest JSON format" - {
    "must check every generated valid request" in {
      forAll(notificationRequestGen) { request =>
        Json.toJson(request).validate[NotificationRequest] mustBe JsSuccess(request)
      }
    }
    "must reject JSON when a required field is missing" in {
      forAll(Gen.oneOf("companies", "saos")) { field =>
        forAll(notificationRequestGen) { request =>
          val json = Json.toJson(request).as[JsObject] - field

          json.validate[NotificationRequest] mustBe a[JsError]
        }
      }
    }
    "must reject JSON when companies has the wrong type" in {
      forAll(notificationRequestGen) { request =>
        val json = Json.toJson(request).as[JsObject] ++ Json.obj("companies" -> "not an array")

        json.validate[NotificationRequest] mustBe a[JsError]
      }
    }
    "must reject JSON when saos has the wrong type" in {
      forAll(notificationRequestGen) { request =>
        val json = Json.toJson(request).as[JsObject] ++ Json.obj("saos" -> "not an array")

        json.validate[NotificationRequest] mustBe a[JsError]
      }
    }
  }

  "Company JSON format" - {
    "must check every generated valid company" in {
      forAll(companyGen) { company =>
        Json.toJson(company).validate[Company] mustBe JsSuccess(company)
      }
    }
  }

  "Sao JSON format" - {
    "must check every generated valid SAO" in {
      forAll(saoGen) { sao =>
        Json.toJson(sao).validate[Sao] mustBe JsSuccess(sao)
      }
    }
  }
}
