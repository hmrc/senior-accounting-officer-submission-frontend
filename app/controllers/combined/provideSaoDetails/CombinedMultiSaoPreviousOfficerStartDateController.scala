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

package controllers.combined.provideSaoDetails

import controllers.actions.*
import controllers.routes
import forms.notification.NotificationMultiSaoPreviousOfficerStartDateFormProvider
import models.{JourneySection, Mode}
import navigation.CombinedNavigator
import pages.notification.{
  NotificationMultiSaoPreviousOfficerNamePage,
  NotificationMultiSaoPreviousOfficerStartDatePage
}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.combined.provideSaoDetails.CombinedMultiSaoPreviousOfficerStartDateView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class CombinedMultiSaoPreviousOfficerStartDateController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: CombinedNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    combinedJourneyCheck: CombinedJourneyAction,
    formProvider: NotificationMultiSaoPreviousOfficerStartDateFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: CombinedMultiSaoPreviousOfficerStartDateView
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode, saoIndex: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen combinedJourneyCheck) { implicit request =>
      request.userAnswers
        .get(NotificationMultiSaoPreviousOfficerNamePage(saoIndex, mode)) match {
        case Some(saoName) =>
          val form         = formProvider(saoName)
          val preparedForm =
            request.userAnswers
              .get(NotificationMultiSaoPreviousOfficerStartDatePage(saoIndex, mode))
              .fold(form)(form.fill)
          Ok(view(saoName, preparedForm, mode, saoIndex))
        case None =>
          Redirect(routes.JourneyRecoveryController.onPageLoad(section = Some(JourneySection.Combined)))
      }
    }

  def onSubmit(mode: Mode, saoIndex: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen combinedJourneyCheck).async { implicit request =>
      request.userAnswers.get(NotificationMultiSaoPreviousOfficerNamePage(saoIndex, mode)) match {
        case None =>
          Future.successful(
            Redirect(
              routes.JourneyRecoveryController.onPageLoad(section = Some(JourneySection.Combined))
            )
          )
        case Some(saoName) =>
          val form = formProvider(saoName)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(saoName, formWithErrors, mode, saoIndex))),
              value =>
                for {
                  updatedAnswers <- Future
                    .fromTry(
                      request.userAnswers.set(NotificationMultiSaoPreviousOfficerStartDatePage(saoIndex, mode), value)
                    )
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator
                    .nextPage(NotificationMultiSaoPreviousOfficerStartDatePage(saoIndex, mode), mode, updatedAnswers)
                )
            )
      }
    }
}
