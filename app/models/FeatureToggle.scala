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

import config.FeatureConfigSupport
import play.api.Configuration
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox.*

enum FeatureToggle(val key: String, val name: String) {
  override def toString: String = s"features.$key"
  
  case CombinedJourney extends FeatureToggle("combined", "Combined")
}

object FeatureToggle extends FeatureConfigSupport {
  
  def checkboxItems(using config: Configuration): Seq[CheckboxItem] =
    FeatureToggle.values.toSeq.zipWithIndex.map { case (value, index) =>
      CheckboxItem(
        content = Text(value.name),
        id = Some(value.key),
        name = Some(value.key),
        value = index.toString,
        checked = isEnabled(value)
      )
    }
}
