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

package controllers

import config.AppConfig
import connectors.{Reference, UpscanInitiateConnector}
import controllers.actions.IdentifierAction
import models.UploadId
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UploadProgressTracker
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.NotificationUploadFormView

import scala.concurrent.ExecutionContext

import javax.inject.Inject

class NotificationUploadFormController @Inject() (
    identify: IdentifierAction,
    mcc: MessagesControllerComponents,
    appConfig: AppConfig,
    notificationUploadFormView: NotificationUploadFormView,
    upscanInitiateConnector: UpscanInitiateConnector,
    uploadProgressTracker: UploadProgressTracker
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = identify async { implicit request =>
    val uploadId = UploadId.generate() // generating the upload id here

    val successRedirectUrl = appConfig.hubBaseUrl + s"?uploadId=${uploadId.value}"
    val errorRedirectUrl   = appConfig.hubBaseUrl

    for
      upscanInitiateResponse <- upscanInitiateConnector.initiateV2(Some(successRedirectUrl), Some(errorRedirectUrl))
      _ <- uploadProgressTracker.requestUpload(uploadId, Reference(upscanInitiateResponse.fileReference.reference))
    yield Ok(notificationUploadFormView(upscanInitiateResponse)) // generate the upload form
  }
}
