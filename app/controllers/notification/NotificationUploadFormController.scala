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

package controllers.notification

import connectors.UpscanInitiateConnector
import controllers.actions.*
import forms.UploadFormProvider
import forms.UploadFormProvider.*
import models.*
import models.upscan.{FileUploadState, UploadJourney, UploadStatus}
import pages.notification.NotificationUploadStatePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.notification.NotificationUploadFormView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class NotificationUploadFormController @Inject() (
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    requireNotificationUploadUnlocked: RequireNotificationUploadUnlockedAction,
    mcc: MessagesControllerComponents,
    notificationUploadFormView: NotificationUploadFormView,
    upscanInitiateConnector: UpscanInitiateConnector,
    sessionRepository: SessionRepository,
    formProvider: UploadFormProvider
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireNotificationUploadUnlocked).async { implicit request =>
      val syncError = for {
        codes <- request.queryString.get(ERROR_CODE_QUERY)
        code  <- codes.headOption
      } yield syncErrorKey(code)

      val asyncError = request.userAnswers.get(NotificationUploadStatePage).collect {
        case FileUploadState(_, UploadStatus.Quarantined)    => "upload.error.quarantine"
        case FileUploadState(_, UploadStatus.Rejected)       => "upload.error.rejected"
        case FileUploadState(_, UploadStatus.UnknownFailure) => "upload.error.unknown"
      }

      val form = syncError
        .orElse(asyncError)
        .fold(formProvider())(err => formProvider().withError(fileInputField, err))

      for {
        upscanInitiateResponse <- upscanInitiateConnector.initiateV2(UploadJourney.Notification)
        updatedAnswers         <- Future.fromTry(
          request.userAnswers.set(
            NotificationUploadStatePage,
            FileUploadState(
              reference = upscanInitiateResponse.reference,
              status = UploadStatus.InProgress
            )
          )
        )
        _ <- sessionRepository.set(updatedAnswers)
      } yield {
        if form.hasErrors then BadRequest(notificationUploadFormView(form, upscanInitiateResponse))
        else Ok(notificationUploadFormView(form, upscanInitiateResponse))
      }
    }
}
