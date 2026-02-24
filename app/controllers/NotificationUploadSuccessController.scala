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
import pages.NotificationUploadReferencePage
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import services.UpscanService
import services.UpscanService.State
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.NotificationUploadSuccessView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class NotificationUploadSuccessController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    upscanService: UpscanService,
    sessionRepository: SessionRepository,
    view: NotificationUploadSuccessView
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(key: Option[String]): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      upscanService.fileUploadState(key.get).flatMap {
        case State.NoReference =>
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case State.WaitingForUpscan =>
          Future.successful(Ok(view()))
        case State.UploadToUpscanFailed =>
          ???
        case State.DownloadFromUpscanFailed(response) =>
          ???
        case State.Result(reference, fileContent) =>
          Logger(getClass).info(fileContent)
          Future
            .fromTry(request.userAnswers.set(NotificationUploadReferencePage, reference))
            .flatMap(sessionRepository.set)
            .map(_ => Redirect(routes.SubmitNotificationStartController.onPageLoad()))
      }
  }

}
