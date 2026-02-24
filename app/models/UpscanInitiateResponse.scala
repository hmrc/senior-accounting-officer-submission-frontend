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

import play.api.libs.functional.syntax.*
import play.api.libs.json.{Format, JsPath}

/** The response received from upscan when the initiate api is called to initiate a file upload
  */
final case class UpscanInitiateResponse(
    reference: String,
    postTarget: String,
    formFields: Map[String, String]
)

object UpscanInitiateResponse {
  given Format[UpscanInitiateResponse] = (
    (JsPath \ "reference").format[String] and
      (JsPath \ "uploadRequest" \ "href").format[String] and
      (JsPath \ "uploadRequest" \ "fields").format[Map[String, String]]
  )(UpscanInitiateResponse.apply _, o => Tuple.fromProductTyped(o))
}
