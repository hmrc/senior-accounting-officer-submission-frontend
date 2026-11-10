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

import config.AppConfig
import controllers.actions.*
import controllers.notification.routes as notificationRoutes
import models.notification.NotificationStage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.NotificationSubmitService
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.notification.NotificationTaskListView

import scala.concurrent.ExecutionContext

import javax.inject.Inject

class NotificationTaskListController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: NotificationTaskListView,
    notificationSubmitService: NotificationSubmitService,
    requireSubmitNotificationUnlocked: RequireSubmitNotificationUnlockedAction,
    appConfig: AppConfig
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    Ok(view(NotificationStage.taskListStage(request.userAnswers)))
  }

  def onComplete: Action[AnyContent] = identify { implicit request =>
    Ok(view(NotificationStage.AllStagesCompleted))
  }

  def onCompleteSubmit: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireSubmitNotificationUnlocked).async { implicit request =>
      if appConfig.faultToleranceEnabled then {
        notificationSubmitService
          .submitWithFaultTolerance(request.userAnswers)
          .map {
            _.fold(
              error => throw new InternalServerException(error.message),
              idempotencyKey => Redirect(notificationRoutes.NotificationSubmittingController.onPageLoad(idempotencyKey))
            )
          }
      } else {
        notificationSubmitService
          .submit(request.userAnswers)
          .map {
            _.fold(
              error => throw new InternalServerException(error.message),
              notificationReference =>
                Redirect(notificationRoutes.NotificationConfirmationController.onPageLoad(notificationReference))
            )
          }
      }
    }
}
