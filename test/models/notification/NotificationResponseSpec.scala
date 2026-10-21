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
import play.api.libs.json.{JsError, JsSuccess, Json}

class NotificationResponseSpec extends SpecBase {

  "NotificationResponse" - {

    "must write to and read from JSON" in {
      val response = NotificationResponse("CRT0123456789")
      val json     = Json.obj("notificationRef" -> "CRT0123456789")

      Json.toJson(response) mustBe json
      json.validate[NotificationResponse] mustBe JsSuccess(response)
    }

    "must fail to read when notificationRef is missing" in {
      Json.obj().validate[NotificationResponse] mustBe a[JsError]
    }
  }
}
