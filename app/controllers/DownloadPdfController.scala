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

import config.ErrorHandler
import controllers.actions.*
import models.SubmissionDocumentType
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import services.ObjectStoreService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class DownloadPdfController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    val controllerComponents: MessagesControllerComponents,
    objectStoreService: ObjectStoreService,
    errorHandler: ErrorHandler
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def notification(notificationReference: String): Action[AnyContent] =
    download(notificationReference, SubmissionDocumentType.Notification)

  def certificate(certificateReference: String): Action[AnyContent] =
    download(certificateReference, SubmissionDocumentType.Certificate)

  private def download(reference: String, documentType: SubmissionDocumentType): Action[AnyContent] =
    identify.async { implicit request =>
      objectStoreService.downloadSubmissionPdf(request.saoSubscriptionId, reference, documentType).flatMap {
        case Some(pdf) =>
          Future.successful(
            Ok.streamed(
              content = pdf.content,
              contentLength = Some(pdf.metadata.contentLength),
              contentType = Some(pdf.metadata.contentType)
            ).withHeaders(
              Results
                .contentDispositionHeader(inline = false, name = Some(documentType.pdfFileName(reference)))
                .toList*
            )
          )
        case None =>
          logger.info(s"[DownloadPdf][NotFound] no $documentType pdf in object store for reference $reference")
          notFound
      }
    }

  private def notFound(using request: RequestHeader): Future[Result] =
    errorHandler.notFoundTemplate.map(NotFound(_))
}
