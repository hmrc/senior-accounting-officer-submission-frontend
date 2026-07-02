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

package controllers.certificate

import connectors.UpscanInitiateConnector
import controllers.actions.*
import forms.UploadFormProvider
import forms.UploadFormProvider.fileInputField
import models.upscan.{FileUploadState, UploadJourney, UploadStatus}
import pages.certificate.CertificateUploadStatePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.certificate.CertificateUploadFormView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class CertificateUploadFormController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    mcc: MessagesControllerComponents,
    certificateUploadFormView: CertificateUploadFormView,
    upscanInitiateConnector: UpscanInitiateConnector,
    sessionRepository: SessionRepository,
    formProvider: UploadFormProvider,
    requireUploadSubmissionTemplateStageUnlocked: RequireCertificateUploadSubmissionTemplateUnlockedAction
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireUploadSubmissionTemplateStageUnlocked).async {
      implicit request =>
        val form = request.userAnswers.get(CertificateUploadStatePage).fold(formProvider()) {
          case FileUploadState(_, UploadStatus.Quarantined) =>
            formProvider().withError(
              fileInputField,
              "upload.error.quarantine"
            )
          case FileUploadState(_, UploadStatus.Rejected) =>
            formProvider().withError(fileInputField, "upload.error.rejected")
          case FileUploadState(_, UploadStatus.UnknownFailure) =>
            formProvider().withError(fileInputField, "upload.error.unknown")
          case _ => formProvider()
        }

        for {
          upscanInitiateResponse <- upscanInitiateConnector.initiateV2(UploadJourney.Certificate)
          updatedAnswers         <- Future.fromTry(
            request.userAnswers.set(
              CertificateUploadStatePage,
              FileUploadState(
                reference = upscanInitiateResponse.reference,
                status = UploadStatus.InProgress
              )
            )
          )
          _ <- sessionRepository.set(updatedAnswers)
        } yield {
          if form.hasErrors then BadRequest(certificateUploadFormView(form, upscanInitiateResponse))
          else Ok(certificateUploadFormView(form, upscanInitiateResponse))
        }
    }
}
