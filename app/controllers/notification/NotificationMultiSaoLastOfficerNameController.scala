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
import forms.notification.NotificationMultiSaoLastOfficerNameFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.notification.{MoreSaoSubmitNotificationFullNamePage, NotificationMultiSaoLastOfficerNamePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.notification.NotificationMultiSaoLastOfficerNameView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class NotificationMultiSaoLastOfficerNameController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: NotificationMultiSaoLastOfficerNameFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: NotificationMultiSaoLastOfficerNameView
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  private def saoNameForPage(saoIndex: Int, userAnswers: UserAnswers): Option[String] =
    if saoIndex == 0 then {
      userAnswers.get(MoreSaoSubmitNotificationFullNamePage)
    } else {
      userAnswers.get(NotificationMultiSaoLastOfficerNamePage(saoIndex - 1))
    }

  def onPageLoad(mode: Mode, saoIndex: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm =
        request.userAnswers.get(NotificationMultiSaoLastOfficerNamePage(saoIndex)).fold(form)(form.fill)
      saoNameForPage(saoIndex, request.userAnswers)
        .fold(
          Redirect(routes.JourneyRecoveryController.onPageLoad())
        )(saoName => Ok(view(saoName, preparedForm, mode, saoIndex)))

  }

  def onSubmit(mode: Mode, saoIndex: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      saoNameForPage(saoIndex, request.userAnswers) match {
        case None          => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case Some(saoName) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(saoName, formWithErrors, mode, saoIndex))),

              value =>
                for {
                  updatedAnswers <- Future
                    .fromTry(request.userAnswers.set(NotificationMultiSaoLastOfficerNamePage(saoIndex), value))
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator.nextPage(NotificationMultiSaoLastOfficerNamePage(saoIndex), mode, updatedAnswers)
                )
            )
      }

  }
}
