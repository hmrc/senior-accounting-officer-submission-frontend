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

package config

import play.api.Configuration

import FeatureToggleConstants.*
import models.FeatureToggle

object FeatureToggleConstants {
  val FEATURE_SWITCH_ON  = "true"
  val FEATURE_SWITCH_OFF = "false"
}

trait FeatureToggleSupport {

  def enable(featureSwitch: FeatureToggle): Unit =
    sys.props += featureSwitch.toString -> FEATURE_SWITCH_ON: Unit

  def disable(featureSwitch: FeatureToggle): Unit =
    sys.props += featureSwitch.toString -> FEATURE_SWITCH_OFF: Unit

}

trait FeatureConfigSupport {
  def isEnabled(featureSwitch: FeatureToggle)(using config: Configuration): Boolean = {
    val key = featureSwitch.toString
    sys.props
      .get(key)
      .getOrElse(config.getOptional[String](key).getOrElse(FEATURE_SWITCH_OFF))
      .toBoolean
  }
}
