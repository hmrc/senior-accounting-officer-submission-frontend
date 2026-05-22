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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

enum CombinedWhoSubmitsCertificate(override val toString: String) {
  case Sao   extends CombinedWhoSubmitsCertificate("sao")
  case Proxy extends CombinedWhoSubmitsCertificate("proxy")
}

object CombinedWhoSubmitsCertificate extends Enumerable.Implicits[CombinedWhoSubmitsCertificate] {

  override def members: Array[CombinedWhoSubmitsCertificate] = CombinedWhoSubmitsCertificate.values

  def options(using messages: Messages): Seq[RadioItem] = values.map { value =>
    RadioItem(
      content = Text(messages(s"combinedWhoSubmitsCertificate.${value.toString}")),
      value = Some(value.toString),
      id = Some(s"value_${value.ordinal}")
    )
  }

}
