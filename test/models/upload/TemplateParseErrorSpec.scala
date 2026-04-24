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
import play.api.libs.json.Json

class TemplateParseErrorSpec extends SpecBase {

  "TemplateParseError json format" - {

    "must round-trip with a column value" in {
      val error = TemplateParseError(
        line = 9,
        column = Some("Company UTR"),
        code = "invalid_company_utr",
        message = "Company UTR must be 10 digits"
      )

      Json.toJson(error).validate[TemplateParseError].get mustBe error
    }

    "must round-trip without a column value" in {
      val error = TemplateParseError(
        line = 8,
        column = None,
        code = "missing_header_row",
        message = "The selected file must use the template"
      )

      Json.toJson(error).validate[TemplateParseError].get mustBe error
    }
  }
}
