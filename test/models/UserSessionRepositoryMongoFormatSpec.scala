/*
 * Copyright 2025 HM Revenue & Customs
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

import base.SpecBase
import play.api.libs.json.{JsError, JsString, Json}

class UserSessionRepositoryMongoFormatSpec extends SpecBase {

  "UserSessionRepository.mongoFormat must" - {

    "correctly write and read UploadStatus.InProgress" in {
      val status: UploadStatus = UploadStatus.InProgress
      val json                 = Json.toJson(status)
      json mustBe Json.obj("statusType" -> "InProgress")
      val result = Json.fromJson[UploadStatus](json)
      result.get mustBe status
    }

    "correctly write and read UploadStatus.Failed" in {
      val status: UploadStatus = UploadStatus.Failed
      val json                 = Json.toJson(status)
      json mustBe Json.obj("statusType" -> "Failed")
      val result = Json.fromJson[UploadStatus](json)
      result.get mustBe status
    }

    "correctly write and read UploadStatus.UploadedSuccessfully" in {
      val status: UploadStatus =
        UploadStatus.UploadedSuccessfully("name", "mimetype", "url", Some(123))
      val json = Json.toJson(status)
      json mustBe Json.obj(
        "name"        -> "name",
        "mimeType"    -> "mimetype",
        "downloadUrl" -> "url",
        "size"        -> 123L,
        "statusType"  -> "UploadedSuccessfully"
      )
      val result =
        Json.fromJson[UploadStatus](json)
      result.get mustBe status
    }

    "return a JsError when reading an invalid statusType" in {
      val json   = Json.obj("statusType" -> "Invalid")
      val result = Json.fromJson[UploadStatus](json)
      result mustBe a[JsError]
    }

    "return a JsError when statusType is unexpected value" in {
      val json   = Json.obj("statusType" -> JsString("UNEXPECTED_VALUE"))
      val result = Json.fromJson[UploadStatus](json)
      result mustBe a[JsError]
    }

    "return a JsError when statusType is missing" in {
      val json   = Json.obj("foo" -> "bar")
      val result = Json.fromJson[UploadStatus](json)
      result mustBe a[JsError]
    }

    "return a JsError when reading a non-JsObject" in {
      val json   = Json.arr("foo" -> "bar")
      val result = Json.fromJson[UploadStatus](json)
      result mustBe a[JsError]
    }

  }
}
