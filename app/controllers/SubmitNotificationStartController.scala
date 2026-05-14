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
import models.{SubmitNotificationStage, UserAnswers}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SubmitNotificationStartView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class SubmitNotificationStartController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    val controllerComponents: MessagesControllerComponents,
    view: SubmitNotificationStartView,
    sessionRepository: SessionRepository
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData) async { implicit request =>
    for {
      userAnswers <- sessionRepository.get(request.userId)
      result      <- userAnswers.fold {
        val answers = UserAnswers(request.userId)
        sessionRepository.set(answers).map(_ => Ok(view(SubmitNotificationStage.from(answers))))
      }(answers => Future.successful(Ok(view(SubmitNotificationStage.from(answers)))))
    } yield result
  }
}
