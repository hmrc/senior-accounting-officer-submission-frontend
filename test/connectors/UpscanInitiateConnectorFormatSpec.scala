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
import models.*
import play.api.libs.json.JsValue
import play.api.libs.json.{JsError, JsSuccess, Json}

class UpscanInitiateConnectorFormatSpec extends SpecBase {

  val initiateRequestModel: UpscanInitiateRequestV2 = UpscanInitiateRequestV2(
    callbackUrl = "localhost:8080//callbackUrl",
    successRedirect = Some("localhost:8080//successUrl"),
    errorRedirect = Some("localhost:8080//errorUrl"),
    minimumFileSize = Some(10),
    maximumFileSize = Some(4096)
  )

  val initiateRequestJson: JsValue = Json.parse(
    """{
      |  "callbackUrl": "localhost:8080//callbackUrl",
      |  "successRedirect": "localhost:8080//successUrl",
      |  "errorRedirect": "localhost:8080//errorUrl",
      |  "minimumFileSize": 10,
      |  "maximumFileSize": 4096
      |}""".stripMargin
  )

  val preparedUploadModel: PreparedUpload = PreparedUpload(
    reference = UpscanFileReference("ref-123"),
    uploadRequest = UploadForm(
      href = "upload-url",
      fields = Map(
        "field1" -> "value1",
        "field2" -> "value2"
      )
    )
  )

  val preparedUploadJson: JsValue = Json.parse(
    """{
      | "reference": "ref-123",
      | "uploadRequest":{
      |   "href":"upload-url",
      |   "fields":{
      |     "field1":"value1",
      |     "field2":"value2"
      |   }
      | }
      |}""".stripMargin
  )

  "UpscanInitiateRequestV2" must {

    "must be converted to JSON correctly (writes)" in {
      Json.toJson(initiateRequestModel) mustEqual initiateRequestJson
    }

    "must be converted from JSON correctly (reads)" in {
      initiateRequestJson.validate[UpscanInitiateRequestV2] mustEqual JsSuccess(initiateRequestModel)
    }

    "must fail to parse when a required field is missing" in {
      val incompleteJson = Json.parse("""{ "errorRedirect": "localhost:8080//errorUrl" }""")
      val result         = incompleteJson.validate[UpscanInitiateRequestV2]

      result match {
        case JsError(_)      => succeed // Test passes if it fails
        case JsSuccess(_, _) => fail("Parsing should have failed due to a missing 'checksum' field")
      }
    }
  }

  "PreparedUpload" must {
    "correctly read JSON" in {
      val result = Json.fromJson[PreparedUpload](preparedUploadJson)
      result.get mustBe preparedUploadModel
    }
  }
}
