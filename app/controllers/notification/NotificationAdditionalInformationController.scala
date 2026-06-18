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
import forms.notification.NotificationAdditionalInformationFormProvider
import models.Mode
import navigation.Navigator
import pages.notification.NotificationAdditionalInformationPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.*
import play.api.libs.json.Reads.*
import play.api.libs.json.Writes.*
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.notification.NotificationAdditionalInformationView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class NotificationAdditionalInformationController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    requireSubmitNotificationUnlocked: RequireSubmitNotificationUnlockedAction,
    formProvider: NotificationAdditionalInformationFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: NotificationAdditionalInformationView
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Option[String]] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireSubmitNotificationUnlocked) { implicit request =>
      val preparedForm =
        request.userAnswers
          .getNullable(NotificationAdditionalInformationPage)
          .fold(form)(value => form.fill(Some(value)))

      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireSubmitNotificationUnlocked).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future
                .fromTry(request.userAnswers.set(NotificationAdditionalInformationPage, value))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(NotificationAdditionalInformationPage, mode, updatedAnswers))
        )
    }
}
