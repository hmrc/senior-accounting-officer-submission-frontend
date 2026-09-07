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
import forms.notification.NotificationMoreThanOneSaoFormProvider
import models.Mode
import navigation.NotificationNavigator
import pages.notification.NotificationMoreThanOneSaoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.notification.NotificationMoreThanOneSaoView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject
import models.UserAnswers
import pages.notification.NotificationMultiSaoAreAllAddedPage
import pages.notification.NotificationMultiSaoPreviousOfficerEndDatePage
import pages.notification.NotificationMultiSaoPreviousOfficerNamePage
import pages.notification.NotificationMultiSaoPreviousOfficerStartDatePage
import play.api.libs.json.JsArray
import utils.MultiSaoUserAnswerHelpers.finalCompleteSaoIndex
import play.api.libs.json.*
import play.api.libs.json.Reads.*
import play.api.libs.functional.syntax.*
import pages.notification.NotificationSingleSaoOfficerNamePage
import play.api.Logging
import pages.notification.NotificationMultiSaoLastOfficerNamePage
import pages.notification.NotificationMultiSaoLastOfficerStartDatePage

class NotificationMoreThanOneSaoController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: NotificationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: NotificationMoreThanOneSaoFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: NotificationMoreThanOneSaoView
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(NotificationMoreThanOneSaoPage).fold(form)(form.fill)
    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(NotificationMoreThanOneSaoPage, value))
              cleanedAnswers = fixupUserAnswers(updatedAnswers)
              _ <- sessionRepository.set(cleanedAnswers)
            } yield Redirect(navigator.nextPage(NotificationMoreThanOneSaoPage, mode, cleanedAnswers))
        )
  }

  def fixupUserAnswers(userAnswers: UserAnswers): UserAnswers = {
    val singleSaoNameKey         = NotificationSingleSaoOfficerNamePage.toString
    val multiSaoLastNameKey      = NotificationMultiSaoLastOfficerNamePage.toString
    val multiSaoLastStartDateKey = NotificationMultiSaoLastOfficerStartDatePage.toString
    val multiSaoNameKey          = NotificationMultiSaoPreviousOfficerNamePage(0).key
    val multiSaoStartDateKey     = NotificationMultiSaoPreviousOfficerStartDatePage(0).key
    val multiSaoEndDateKey       = NotificationMultiSaoPreviousOfficerEndDatePage(0).key
    val multiSaoAddedAllKey      = NotificationMultiSaoAreAllAddedPage(0).key

    userAnswers.get(NotificationMoreThanOneSaoPage) match {
      case Some(true) => {
        val transformer = (__ \ "notification" \ singleSaoNameKey).json.prune

        userAnswers.data.transform(transformer) match {
          case JsError(errors)           => ???
          case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
        }
      }
      case Some(false) => {
        val transformer =
          (__ \ "notification" \ multiSaoLastNameKey).json.prune andThen
            (__ \ "notification" \ multiSaoLastStartDateKey).json.prune andThen
            (__ \ "notification" \ multiSaoNameKey).json.prune andThen
            (__ \ "notification" \ multiSaoStartDateKey).json.prune andThen
            (__ \ "notification" \ multiSaoEndDateKey).json.prune andThen
            (__ \ "notification" \ multiSaoAddedAllKey).json.prune

        userAnswers.data.transform(transformer) match {
          case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
          case JsError(_)                => ???
        }
      }
      case None => ???
    }
  }
}
