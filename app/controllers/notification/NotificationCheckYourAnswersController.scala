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
import utils.MultiSaoUserAnswerHelpers.finalCompleteSaoIndex
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
    notificationSubmitService: NotificationSubmitService
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireSubmitNotificationUnlocked).async { implicit request =>
      val fixedUserAnswers = fixupUserAnswers(request.userAnswers)
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

  def fixupUserAnswers(userAnswers: UserAnswers): UserAnswers = {
    val multiSaoNameKey      = NotificationMultiSaoPreviousOfficerNamePage(0).key
    val multiSaoStartDateKey = NotificationMultiSaoPreviousOfficerStartDatePage(0).key
    val multiSaoEndDateKey   = NotificationMultiSaoPreviousOfficerEndDatePage(0).key
    val multiSaoAddedAllKey  = NotificationMultiSaoAreAllAddedPage(0).key

    userAnswers.get(NotificationMoreThanOneSaoPage) match {
      case Some(true) => {
        val finalIndex = finalCompleteSaoIndex(userAnswers)

        val takeFromArray = of[JsArray].map { case JsArray(contents) => JsArray(contents.take(finalIndex + 1)) }

        val transformer = (__ \ "notification").json.update(
          (__ \ multiSaoNameKey).json.update(takeFromArray)
            andThen (__ \ multiSaoStartDateKey).json.update(takeFromArray)
            andThen (__ \ multiSaoEndDateKey).json.update(takeFromArray)
            andThen (__ \ multiSaoAddedAllKey).json.update(takeFromArray)
            andThen (__ \ multiSaoAddedAllKey).json.update(of[JsArray].map { case JsArray(contents) =>
              JsArray(contents.dropRight(1) :+ JsTrue)
            })
            andThen (__ \ NotificationSingleSaoOfficerNamePage.toString).json.prune
        )

        userAnswers.data.transform(transformer) match {
          case JsError(_)                => ???
          case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
        }
      }
      case Some(false) => {
        val transformer = (__ \ "notification").json.update(
          (__ \ multiSaoNameKey).json.prune
            andThen (__ \ multiSaoStartDateKey).json.prune
            andThen (__ \ multiSaoEndDateKey).json.prune
            andThen (__ \ multiSaoAddedAllKey).json.prune
        )

        userAnswers.data.transform(transformer) match {
          case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
          case JsError(_)                => ???
        }
      }
      case None => ???
    }
  }
}
