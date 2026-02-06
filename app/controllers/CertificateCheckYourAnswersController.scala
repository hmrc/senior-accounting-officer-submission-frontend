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

package controllers

import controllers.actions.*
import models.NormalMode
import navigation.Navigator
import pages.{CertificateCheckYourAnswersPage, IsThisTheSaoOnCertificatePage, SaoNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.CertificateCheckYourAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CertificateCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import javax.inject.Inject

class CertificateCheckYourAnswersController @Inject (
    sessionRepo: SessionRepository
)(
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: CertificateCheckYourAnswersView,
    navigator: Navigator,
    certificateCheckYourAnswersService: CertificateCheckYourAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val userAnswers = request.userAnswers
    val summaryList = certificateCheckYourAnswersService.getSummaryList(userAnswers)

    Ok(view(summaryList))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    Redirect(navigator.nextPage(CertificateCheckYourAnswersPage, NormalMode, request.userAnswers))
  }
}
