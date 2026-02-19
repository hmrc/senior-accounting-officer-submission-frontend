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

import connectors.UpscanInitiateConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.*
import org.bson.types.ObjectId
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.UpscanSessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.NotificationUploadFormView

import scala.concurrent.ExecutionContext

import javax.inject.Inject

class NotificationUploadFormController @Inject() (
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    mcc: MessagesControllerComponents,
    notificationUploadFormView: NotificationUploadFormView,
    upscanInitiateConnector: UpscanInitiateConnector,
    upscanSessionRepository: UpscanSessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    val uploadId = UploadId.generate()
    for
      upscanInitiateResponse <- upscanInitiateConnector.initiateV2(uploadId.value)
      _                      <- upscanSessionRepository.insert(
        FileUploadState(
          ObjectId.get(),
          uploadId,
          UpscanFileReference(upscanInitiateResponse.fileReference.reference),
          UploadStatus.InProgress
        )
      )
    yield Ok(notificationUploadFormView(upscanInitiateResponse))
  }
}
