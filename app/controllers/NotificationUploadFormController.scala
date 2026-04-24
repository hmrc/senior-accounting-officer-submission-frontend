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
import controllers.NotificationUploadFormController.fileInputField
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.NotificationUploadFormProvider
import models.*
import pages.NotificationUploadStatePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.NotificationUploadFormView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class NotificationUploadFormController @Inject() (
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    mcc: MessagesControllerComponents,
    notificationUploadFormView: NotificationUploadFormView,
    upscanInitiateConnector: UpscanInitiateConnector,
    sessionRepository: SessionRepository,
    formProvider: NotificationUploadFormProvider
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    val form = request.userAnswers.get(NotificationUploadStatePage).fold(formProvider()) {
      case NotificationUploadState(_, UploadStatus.Quarantined) =>
        formProvider().withError(
          fileInputField,
          "notificationUploadForm.upload.error.quarantine"
        )
      case NotificationUploadState(_, UploadStatus.Rejected) =>
        formProvider().withError(fileInputField, "notificationUploadForm.upload.error.rejected")
      case NotificationUploadState(_, UploadStatus.UnknownFailure) =>
        formProvider().withError(fileInputField, "notificationUploadForm.upload.error.unknown")
      case _ => formProvider()
    }

    for {
      upscanInitiateResponse <- upscanInitiateConnector.initiateV2()
      updatedAnswers         <- Future.fromTry(
        request.userAnswers.set(
          NotificationUploadStatePage,
          NotificationUploadState(
            reference = upscanInitiateResponse.reference,
            status = UploadStatus.InProgress
          )
        )
      )
      _ <- sessionRepository.set(updatedAnswers)
    } yield Ok(notificationUploadFormView(form, upscanInitiateResponse))
  }
}

object NotificationUploadFormController {
  val fileInputField = "file"
}
