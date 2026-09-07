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
import forms.notification.NotificationMultiSaoAreAllAddedFormProvider
import models.Mode
import navigation.NotificationNavigator
import pages.notification.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.notification.NotificationMultiSaoAreAllAddedView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject
import models.UserAnswers
import scala.annotation.tailrec
import pages.notification.NotificationMultiSaoPreviousOfficerNamePage
import play.api.libs.json.*
import play.api.libs.json.Reads.*
import play.api.libs.functional.syntax.*

class NotificationMultiSaoAreAllAddedController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: NotificationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: NotificationMultiSaoAreAllAddedFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: NotificationMultiSaoAreAllAddedView
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  def onPageLoad(mode: Mode, saoIndex: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form         = formProvider()
      val preparedForm = request.userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex)).fold(form)(form.fill)
      Ok(view(preparedForm, mode, saoIndex))
  }

  def onSubmit(mode: Mode, saoIndex: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider()
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, saoIndex))),
          value =>
            for {
              updatedAnswers <- Future
                .fromTry(request.userAnswers.set(NotificationMultiSaoAreAllAddedPage(saoIndex), value))
              cleanedAnswers = cleanupSaoData(updatedAnswers, saoIndex)
              _ <- sessionRepository.set(cleanedAnswers)
            } yield Redirect(navigator.nextPage(NotificationMultiSaoAreAllAddedPage(saoIndex), mode, cleanedAnswers))
        )
  }

  def cleanupSaoData(userAnswers: UserAnswers, saoIndex: Int): UserAnswers = {
    val userAnsweredYes = userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex)) == Some(true)

    if userAnsweredYes then {

      val nameKey      = NotificationMultiSaoPreviousOfficerNamePage(0).key
      val startDateKey = NotificationMultiSaoPreviousOfficerStartDatePage(0).key
      val endDateKey   = NotificationMultiSaoPreviousOfficerEndDatePage(0).key
      val addedAllKey  = NotificationMultiSaoAreAllAddedPage(0).key

      val takeFromArray = of[JsArray].map { case JsArray(contents) => JsArray(contents.take(saoIndex + 1)) }

      val transformer = (__ \ "notification").json.update(
        (__ \ nameKey).json.update(takeFromArray) andThen
          (__ \ startDateKey).json.update(takeFromArray) andThen
          (__ \ endDateKey).json.update(takeFromArray) andThen
          (__ \ addedAllKey).json.update(takeFromArray)
      )

      userAnswers.data.transform(transformer) match {
        case JsError(_)                => ???
        case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
      }
    } else {
      userAnswers
    }
  }
}
