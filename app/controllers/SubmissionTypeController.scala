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

import config.FeatureConfigSupport
import controllers.actions.*
import forms.SubmissionTypeFormProvider
import models.{NormalMode, SubmissionType, UserAnswers}
import navigation.AgnosticNavigator
import pages.SubmissionTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SubmissionTypeView
import play.api.Configuration
import models.FeatureToggle
import models.FeatureToggle.CombinedJourney

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject

class SubmissionTypeController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: AgnosticNavigator,
    identify: IdentifierAction,
    //getData: DataRetrievalAction,
    formProvider: SubmissionTypeFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: SubmissionTypeView
)(using ec: ExecutionContext)(using config : Configuration)
    extends FrontendBaseController
    with I18nSupport 
    with FeatureConfigSupport {
  def onPageLoad(): Action[AnyContent] = (identify) { implicit request =>
    val form         = formProvider()
    //val preparedForm = request.userAnswers.flatMap(_.get(SubmissionTypePage)).fold(form)(form.fill)
    isEnabled(CombinedJourney)
    Ok(view(form, isEnabled(CombinedJourney)))
  }

  def onSubmit(): Action[AnyContent] = (identify).async { implicit request =>
    val form = formProvider()
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, isEnabled(CombinedJourney)))),
        value =>
          for {
            _ <- sessionRepository.set(UserAnswers("yes"))
          } yield Redirect(navigator.nextPage(SubmissionTypePage, NormalMode, UserAnswers("yes")))
      )
  }
}
