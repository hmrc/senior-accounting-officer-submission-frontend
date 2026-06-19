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
import controllers.routes
import forms.notification.NotificationMultiSaoLastOfficerStartDateFormProvider
import models.Mode
import navigation.Navigator
import pages.notification.{NotificationMultiSaoLastOfficerNamePage, NotificationMultiSaoLastOfficerStartDatePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.notification.NotificationMultiSaoLastOfficerStartDateView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class NotificationMultiSaoLastOfficerStartDateController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: NotificationMultiSaoLastOfficerStartDateFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: NotificationMultiSaoLastOfficerStartDateView
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val form         = formProvider()
    val preparedForm = request.userAnswers.get(NotificationMultiSaoLastOfficerStartDatePage).fold(form)(form.fill)
    request.userAnswers
      .get(NotificationMultiSaoLastOfficerNamePage)
      .fold(
        Redirect(routes.JourneyRecoveryController.onPageLoad())
      )(saoName => Ok(view(saoName, preparedForm, mode)))

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider()
      request.userAnswers.get(NotificationMultiSaoLastOfficerNamePage) match {
        case Some(saoName) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(saoName, formWithErrors, mode))),
              value =>
                for {
                  updatedAnswers <- Future
                    .fromTry(request.userAnswers.set(NotificationMultiSaoLastOfficerStartDatePage, value))
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(NotificationMultiSaoLastOfficerStartDatePage, mode, updatedAnswers))
            )

        case None => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}
