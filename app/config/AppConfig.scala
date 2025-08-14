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

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.RequestHeader

@Singleton
class AppConfig @Inject() (config: Configuration) {
  val welshLanguageSupportEnabled: Boolean =
    config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  val host: String = config.get[String]("host")

  private val contactHost                  = config.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = config.get[String]("serviceId")

  val loginUrl: String         = config.get[String]("urls.login")
  val loginContinueUrl: String = config.get[String]("urls.loginContinue")
  val signOutUrl: String       = config.get[String]("urls.signOut")

  private val exitSurveyBaseUrl: String = config.get[String]("feedback-frontend.host")
  val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/$contactFormServiceIdentifier"

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

}
