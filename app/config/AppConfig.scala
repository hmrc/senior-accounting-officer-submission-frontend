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

import controllers.internal.routes
import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (servicesConfig: ServicesConfig, config: Configuration) {

  val cacheTtl: Long = config.get[Int]("mongodb.timeToLiveInSeconds")

  val welshLanguageSupportEnabled: Boolean =
    config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  val host: String = config.get[String]("host")

  private val contactHost                  = config.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = config.get[String]("serviceId")

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  def hubBaseUrl: String         = getValue("hub-frontend.host") + "/senior-accounting-officer"
  val hubSignOutUrl: String      = hubBaseUrl + "/account/sign-out-survey"
  val hubUnauthorisedUrl: String = hubBaseUrl + "/unauthorised"

  val loginContinueUrl: String = hubBaseUrl

  def upscanOurUserAgent: String        = "senior-accounting-officer-submission-frontend"
  lazy val upscanInitiateV2Url: String  = servicesConfig.baseUrl("upscan-initiate") + "/upscan/v2/initiate"
  lazy val upscanDownloadHost: String   = config.get[String]("upscan-download.host")
  lazy val upscanCallbackTarget: String =
    s"${servicesConfig.baseUrl("senior-accounting-officer-submission-frontend")}${routes.UploadCallbackController.callback()}"

  private def getValue(key: String): String =
    sys.props.getOrElse(key, config.get[String](key))

  def templateFile: String = getValue("templateFile")

  val timeout: Int   = config.get[Int]("timeout-dialog.timeout")
  val countdown: Int = config.get[Int]("timeout-dialog.countdown")

}

object AppConfig {
  def setValue(key: String, value: String): Unit =
    sys.props.addOne((key, value)): Unit
}
