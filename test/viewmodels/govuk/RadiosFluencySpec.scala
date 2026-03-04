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

package viewmodels.govuk

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.govukfrontend.views.viewmodels.FormGroup
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import viewmodels.govuk.radios.*

class RadiosFluencySpec extends AnyFreeSpec with Matchers with OptionValues {

  ".withParagraph" - {
    "must escape text content before rendering in HtmlContent" in {
      val inputText = """company's details <script>alert("x")</script>"""
      val radios    = Radios(name = "value", formGroup = FormGroup())

      val result       = radios.withParagraph(inputText)
      val renderedHtml = result.formGroup.beforeInput.value.asHtml.body

      renderedHtml must include("company&#x27;s")
      renderedHtml must include("&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt;")
      renderedHtml must not include "<script>"
    }
  }
}
