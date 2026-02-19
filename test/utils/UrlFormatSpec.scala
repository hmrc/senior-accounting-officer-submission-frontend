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

package utils

import base.SpecBase
import play.api.libs.json.*

import java.net.URL // Assuming this is the model type

class UrlFormatSpec extends SpecBase {

  private val validUrlInstance = new URL("http://example.com/some/path")

  private val validUrlJson = JsString("http://example.com/some/path")

  import utils.HttpUrlFormat.given // Import the implicit format here

  "UrlFormat must" - {
    "correctly write a URL to a JsString" in {
      Json.toJson(validUrlInstance) mustEqual validUrlJson
    }

    "successfully read a valid JsString into a URL" in {
      validUrlJson.validate[URL] mustEqual JsSuccess(validUrlInstance)
    }

    "return an invalid URL error when reading an invalid string" in {
      val invalidUrlJson = JsString("not-a-valid-url")
      val result         = invalidUrlJson.validate[URL]

      result match {
        case JsError(_)      => succeed // We expect a JsError to be returned
        case JsSuccess(_, _) => fail("Reading an invalid URL should have failed")
      }
    }
  }
}
