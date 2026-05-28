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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

enum SubmissionType(override val toString: String) {
  case Notification extends SubmissionType("notification")
  case Certificate extends SubmissionType("certificate")
}

object SubmissionType extends Enumerable.Implicits[SubmissionType] {

  override def members: Array[SubmissionType] = SubmissionType.values

  def options(using messages: Messages): Seq[RadioItem] = values.map { value =>
    RadioItem(
      content = Text(messages(s"submissionType.${value.toString}")),
      value   = Some(value.toString),
      id      = Some(s"value_${value.ordinal}")
    )
  }

}
