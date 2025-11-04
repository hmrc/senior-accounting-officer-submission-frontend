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

package connectors

import base.SpecBase
import play.api.libs.json.Json

class UpscanInitiateConnectorFormatSpec extends SpecBase {

  "UpscanInitiateRequestV2" must {
    "correctly write and read JSON with all fields" in {
      val model = UpscanInitiateRequestV2(
        callbackUrl = "callback",
        successRedirect = Some("success"),
        errorRedirect = Some("error"),
        minimumFileSize = Some(10),
        maximumFileSize = Some(100)
      )
      val json = Json.toJson(model)
      json mustBe Json.obj(
        "callbackUrl"     -> "callback",
        "successRedirect" -> "success",
        "errorRedirect"   -> "error",
        "minimumFileSize" -> 10,
        "maximumFileSize" -> 100
      )
      val result = Json.fromJson[UpscanInitiateRequestV2](json)
      result.get mustBe model
    }
    "correctly write and read JSON with minimum fields" in {
      val model = UpscanInitiateRequestV2(
        callbackUrl = "callback",
        maximumFileSize = None
      )
      val json = Json.toJson(model)
      json mustBe Json.obj(
        "callbackUrl" -> "callback"
      )

      // when deserialising the missing field has a default value
      val expectedModelAfterParse = UpscanInitiateRequestV2(
        callbackUrl = "callback",
        maximumFileSize = Some(4096)
      )

      val result = Json.fromJson[UpscanInitiateRequestV2](json)
      result.get mustBe expectedModelAfterParse
    }
  }

  "PreparedUpload" must {
    "correctly read JSON" in {
      val json = Json.obj(
        "reference"     -> "ref-123",
        "uploadRequest" -> Json.obj(
          "href"   -> "upload-url",
          "fields" -> Json.obj(
            "field1" -> "value1",
            "field2" -> "value2"
          )
        )
      )
      val expectedModel = PreparedUpload(
        reference = Reference("ref-123"),
        uploadRequest = UploadForm(
          href = "upload-url",
          fields = Map(
            "field1" -> "value1",
            "field2" -> "value2"
          )
        )
      )
      val result = Json.fromJson[PreparedUpload](json)
      result.get mustBe expectedModel
    }
  }
}
