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

package viewmodels

import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Key

object converters extends ConverterExtensions

trait ConverterExtensions {

  extension (value: Int) {
    def toText: Text = Text(value.toString)
  }

  extension (string: String) {
    inline def toText: Text =
      Text(string)

    inline def toKey: Key =
      Key(content = Text(string))
  }

  extension (html: Html) {
    inline def toText: Text =
      Text(html.toString)
  }
}
