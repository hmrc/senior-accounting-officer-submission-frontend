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

package controllers.internal

import models.*
import play.api.Logging
import play.api.libs.json.*
import play.api.mvc.*
import services.UpscanCallbackDispatcher
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

import javax.inject.{Inject, Singleton}

@Singleton
class UploadCallbackController @Inject() (
    upscanCallbackDispatcher: UpscanCallbackDispatcher,
    mcc: MessagesControllerComponents
)(using ExecutionContext)
    extends FrontendController(mcc)
    with Logging:

  val callback: Action[JsValue] =
    Action.async(parse.json): request =>
      given Request[JsValue] = request
      logger.info(s"Received callback notification [${Json.stringify(request.body)}]")
      withJsonBody[UpscanCallback]: feedback =>
        upscanCallbackDispatcher.handleCallback(feedback).map(_ => Ok)
