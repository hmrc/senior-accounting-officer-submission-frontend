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

package controllers

import models.UserAnswers
import models.requests.DataRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}

import scala.concurrent.Future

def ensure[B](required: UserAnswers => Option[B])(
    block: DataRequest[AnyContent] => B => Result
): DataRequest[AnyContent] => Result = { request =>
  required(request.userAnswers).fold(
    Redirect(routes.JourneyRecoveryController.onPageLoad())
  )(block(request))
}

def ensureAsync[B](required: UserAnswers => Option[B])(
    block: DataRequest[AnyContent] => B => Future[Result]
): DataRequest[AnyContent] => Future[Result] = { request =>
  required(request.userAnswers).fold(
    Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
  )(block(request))
}
