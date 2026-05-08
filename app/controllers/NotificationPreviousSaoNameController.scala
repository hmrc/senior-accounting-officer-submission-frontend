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

import controllers.actions.*
import forms.NotificationPreviousSaoNameFormProvider
import models.Mode
import navigation.Navigator
import pages.NotificationPreviousSaoNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.NotificationPreviousSaoNameView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class NotificationPreviousSaoNameController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: NotificationPreviousSaoNameFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: NotificationPreviousSaoNameView
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  def onPageLoad(mode: Mode, sao: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form         = formProvider()
      val preparedForm = request.userAnswers.get(NotificationPreviousSaoNamePage(index = sao)).fold(form)(form.fill)
      Ok(view(preparedForm, mode, index = sao))
  }

  def onSubmit(mode: Mode, sao: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider()
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, index = sao))),
          value =>
            for {
              updatedAnswers <- Future
                .fromTry(request.userAnswers.set(NotificationPreviousSaoNamePage(index = sao), value))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(NotificationPreviousSaoNamePage(index = sao), mode, updatedAnswers))
        )
  }
}
