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

import controllers.actions.*
import controllers.notification.routes as notificationRoutes
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{NotificationCheckYourAnswersService, NotificationSubmitService}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.notification.NotificationCheckYourAnswersView

import scala.concurrent.ExecutionContext

import javax.inject.Inject
import models.UserAnswers
import pages.notification.NotificationMultiSaoAreAllAddedPage
import pages.notification.NotificationMultiSaoPreviousOfficerEndDatePage
import pages.notification.NotificationMultiSaoPreviousOfficerNamePage
import pages.notification.NotificationMultiSaoPreviousOfficerStartDatePage
import play.api.libs.json.*
import play.api.libs.json.Reads.*
import play.api.libs.functional.syntax.*
import repositories.SessionRepository
import pages.notification.NotificationMoreThanOneSaoPage
import pages.notification.NotificationSingleSaoOfficerNamePage
import services.SaoUserAnswersService

class NotificationCheckYourAnswersController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    sessionRepository: SessionRepository,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    requireSubmitNotificationUnlocked: RequireSubmitNotificationUnlockedAction,
    val controllerComponents: MessagesControllerComponents,
    view: NotificationCheckYourAnswersView,
    notificationCheckYourAnswersService: NotificationCheckYourAnswersService,
    notificationSubmitService: NotificationSubmitService,
    saoUserAnswersService: SaoUserAnswersService
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireSubmitNotificationUnlocked).async { implicit request =>
      val fixedUserAnswers = saoUserAnswersService.fixupUserAnswers(request.userAnswers)
      for {
        _ <- sessionRepository.set(fixedUserAnswers)
      } yield {
        val summaryList = notificationCheckYourAnswersService.getSummaryList(fixedUserAnswers)

        Ok(view(summaryList, fixedUserAnswers.getFinancialYearEndDate))
      }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireSubmitNotificationUnlocked).async { implicit request =>
      {
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
