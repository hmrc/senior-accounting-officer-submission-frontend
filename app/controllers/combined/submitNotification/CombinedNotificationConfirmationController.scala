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

package controllers.combined.submitNotification

import controllers.actions.*
import models.{NormalMode, SubmissionDocumentType}
import navigation.CombinedNavigator
import pages.combined.CombinedNotificationConfirmationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.PDFAccessService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.combined.submitNotification.CombinedNotificationConfirmationView

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject

class CombinedNotificationConfirmationController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    combinedJourneyCheck: CombinedJourneyAction,
    val controllerComponents: MessagesControllerComponents,
    view: CombinedNotificationConfirmationView,
    pdfAccessService: PDFAccessService,
    navigator: CombinedNavigator,
    sessionRepository: SessionRepository,
    requireSubmitNotificationUnlocked: RequireCombinedSubmitNotificationUnlockedAction
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(notificationReference: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen combinedJourneyCheck andThen requireSubmitNotificationUnlocked)
      .async { implicit request =>
        pdfAccessService.checkPDFStatus(request.saoSubscriptionId, notificationReference, SubmissionDocumentType.Notification).map {
          isPdfAvailable =>
            Ok(
              view(
                notificationReference = notificationReference,
                displayPdfLink = isPdfAvailable
              )
            )
        }
      }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen combinedJourneyCheck andThen requireSubmitNotificationUnlocked)
      .async { implicit request =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(CombinedNotificationConfirmationPage, true))
          _              <- sessionRepository.set(updatedAnswers)
        } yield Redirect(navigator.nextPage(CombinedNotificationConfirmationPage, NormalMode, updatedAnswers))
      }
}
