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
import models.NormalMode
import navigation.Navigator
import pages.CertificateReviewQualifiedPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CertificateReviewQualifiedView

import javax.inject.Inject
import repositories.SessionRepository
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

class CertificateReviewQualifiedController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    navigator: Navigator,
    sessionRepository: SessionRepository,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    priorStagesCompleted: CertificateProvideSaoDetailsStageCompletedAction,
    val controllerComponents: MessagesControllerComponents,
    view: CertificateReviewQualifiedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData andThen priorStagesCompleted) {
    implicit request =>
      Ok(view())
  }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen priorStagesCompleted).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(CertificateReviewQualifiedPage, "HACK"))
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(navigator.nextPage(CertificateReviewQualifiedPage, NormalMode, request.userAnswers))
    }
}
