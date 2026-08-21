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
import models.NormalMode
import models.upload.UploadTemplateTableData
import navigation.NotificationNavigator
import pages.notification.{UploadTemplateReviewPage, UploadTemplateTablePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.UploadTemplatePlaybackService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.notification.{UploadTemplateTableErrorView, UploadTemplateTableView}

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class UploadTemplateTableController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    requireNotificationUploadUnlocked: RequireNotificationUploadUnlockedAction,
    val controllerComponents: MessagesControllerComponents,
    view: UploadTemplateTableView,
    errorView: UploadTemplateTableErrorView,
    playbackService: UploadTemplatePlaybackService,
    navigator: NotificationNavigator,
    sessionRepository: SessionRepository
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireNotificationUploadUnlocked) { implicit request =>
      request.userAnswers
        .get(UploadTemplateTablePage)
        .fold(Redirect(routes.JourneyRecoveryController.onPageLoad())) { tableData =>
          if tableData.hasErrors then {
            Ok(errorView(tableData))
          } else {
            playbackService
              .getPlayback(request.userAnswers)
              .fold(Redirect(notificationRoutes.NotificationTaskListController.onPageLoad())) { playback =>
                Ok(view(playback.tableData, playback.saoName))
              }
          }
        }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireNotificationUploadUnlocked).async { implicit request =>
      request.userAnswers
        .get(UploadTemplateTablePage)
        .fold(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))) {
          case tableData if tableData.hasErrors =>
            Future.successful(Redirect(notificationRoutes.NotificationUploadFormController.onPageLoad()))
          case _ =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(UploadTemplateReviewPage, true))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(UploadTemplateTablePage, NormalMode, updatedAnswers))
        }
    }
}
