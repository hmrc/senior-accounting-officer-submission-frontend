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

package controllers.combined.submitNotification

import com.google.inject.Inject
import controllers.actions.*
import controllers.combined.submitNotification.routes as submitNotificationRoutes
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.NotificationSubmitService
import services.NotificationSubmitService.NotificationState.{Failure, Pending, Success}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.combined.submitNotification.CombinedNotificationSubmittingView

import scala.concurrent.ExecutionContext

class CombinedNotificationSubmittingController @Inject() (
    view: CombinedNotificationSubmittingView,
    notificationSubmitService: NotificationSubmitService,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    combinedJourneyCheck: CombinedJourneyAction,
    requireSubmitNotificationUnlocked: RequireCombinedSubmitNotificationUnlockedAction,
    val controllerComponents: MessagesControllerComponents
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(idempotencyKey: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen combinedJourneyCheck andThen requireSubmitNotificationUnlocked)
      .async { implicit request =>
        notificationSubmitService
          .getStateOfWorkItem(idempotencyKey, request.userAnswers, wipeNotificationData = false)
          .map {
            case Success(notificationRef) =>
              Redirect(submitNotificationRoutes.CombinedNotificationConfirmationController.onPageLoad(notificationRef))
            case Pending(_)      => Ok(view())
            case Failure(status) =>
              throw new InternalServerException(s"Error getting notification state with status $status")
          }
      }
}
