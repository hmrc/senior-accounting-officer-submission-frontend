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

package controllers.notification

import controllers.actions.*
import controllers.notification.routes as notificationRoutes
import controllers.routes
import models.requests.DataRequest
import models.upload.UploadTemplateTableData
import models.upscan.UploadJourney
import pages.notification.UploadTemplateTablePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import services.UpscanService
import services.UpscanService.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.notification.NotificationUploadSuccessView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class NotificationUploadSuccessController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    requireNotificationUploadUnlocked: RequireNotificationUploadUnlockedAction,
    val controllerComponents: MessagesControllerComponents,
    sessionRepository: SessionRepository,
    upscanService: UpscanService,
    view: NotificationUploadSuccessView
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(key: Option[String]): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireNotificationUploadUnlocked).async { implicit request =>
      upscanService.fileUploadState(UploadJourney.Notification, request.userAnswers, key).flatMap {
        case State.NoReference =>
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case State.WaitingForUpscan =>
          Future.successful(Ok(view()))
        case State.QuarantinedByUpscan =>
          Future.successful(Redirect(notificationRoutes.NotificationUploadFormController.onPageLoad()))
        case State.RejectedByUpscan =>
          Future.successful(Redirect(notificationRoutes.NotificationUploadFormController.onPageLoad()))
        case State.UnknownUpscanError =>
          Future.successful(Redirect(notificationRoutes.NotificationUploadFormController.onPageLoad()))
        case State.DownloadFromUpscanFailed(response) =>
          logger.warn(s"Failed to download uploaded template from Upscan: ${response.status}")
          Future.successful(Redirect(notificationRoutes.NotificationUploadFormController.onPageLoad()))
        case State.ValidationFailed(errors) =>
          logger.warn(s"Uploaded template failed validation with ${errors.size} error(s)")
          saveTableDataAndRedirect(
            UploadTemplateTableData(rows = Seq.empty, errors = errors),
            notificationRoutes.UploadTemplateTableErrorController.onPageLoad()
          )
        case State.Result(reference, rows) =>
          logger.info(s"Uploaded template parsed successfully, reference: $reference, rows: ${rows.size}")
          saveTableDataAndRedirect(
            UploadTemplateTableData(rows = rows, errors = Seq.empty),
            notificationRoutes.UploadTemplateTableController.onPageLoad()
          )
      }
    }

  private def saveTableDataAndRedirect(
      tableData: UploadTemplateTableData,
      redirectTo: Call
  )(using request: DataRequest[AnyContent]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(UploadTemplateTablePage, tableData))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(redirectTo)
}
