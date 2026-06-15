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

package controllers.testonly

import connectors.InternalAuthTestOnlyConnector
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.ExecutionContext

import javax.inject.Inject

class InternalAuthTestOnlyController @Inject() (
    mcc: MessagesControllerComponents,
    connector: InternalAuthTestOnlyConnector
)(using ExecutionContext)
    extends FrontendController(mcc) {

  def grantSaoObjectStoreAccess(): Action[AnyContent] = Action.async { implicit request =>
    given hc: uk.gov.hmrc.http.HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    connector.grantSaoObjectStoreAccess().map { response =>
      Status(response.status)(response.body)
    }
  }
}
