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
import pages.notification.SubmissionInProgress
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.NotificationSubmitService
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.notification.NotificationSubmissionInProgressView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class NotificationSubmissionInProgressController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: NotificationSubmissionInProgressView,
    sessionRepository: SessionRepository,
    notificationSubmitService: NotificationSubmitService
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    for {
      correlationId <- request.userAnswers
        .get(SubmissionInProgress)
        .fold(
          Future.failed(
            new InternalServerException(
              "[GetSubmissionStatus] The correlationId for the submission is not found in Mongo"
            )
          )
        )(Future.successful)
      pollingResult <- notificationSubmitService.getSubmissionStatus(correlationId)
      result        <- pollingResult match {
        case Right(Some(submissionId)) =>
          sessionRepository
            .set(request.userAnswers.copy(data = Json.obj()))
            .map(_ => Redirect(routes.NotificationConfirmationController.onPageLoad(submissionId)))
        case Right(None)  => Future.successful(Ok(view()))
        case Left(status) =>
          Future.failed(
            new InternalServerException(
              s"[GetSubmissionStatus][correlationId=$correlationId][UnexpectedStatus=$status]"
            )
          )
      }
    } yield result
  }

}
