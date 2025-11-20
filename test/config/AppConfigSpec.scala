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

import base.SpecBase
import org.scalatest.wordspec.AnyWordSpec

class AppConfigSpec extends SpecBase {
  /*
  lazy val initiateV2Url: String          = servicesConfig.baseUrl("upscan-initiate") + "/upscan/v2/initiate"
  lazy val callbackEndpointTarget: String = config.get[String]("upscan.callback-endpoint")
   */

  lazy val config: AppConfig = app.injector.instanceOf[AppConfig]
  "initiateV2Url" must {
    "return correct intitate end point" in {
      config.initiateV2Url mustBe "http://localhost:9570/upscan/v2/initiate"
    }

    "return correct callback end point" in {
      config.callbackEndpointTarget mustBe "http://localhost:10058/internal/upscan-callback"
    }
  }

}
