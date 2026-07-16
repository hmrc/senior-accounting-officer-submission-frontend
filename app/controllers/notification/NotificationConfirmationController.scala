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
import controllers.notification.NotificationConfirmationController.*
import models.NormalMode
import navigation.Navigator
import pages.notification.NotificationConfirmationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.notification.NotificationConfirmationView

import scala.concurrent.ExecutionContext

import javax.inject.Inject

class NotificationConfirmationController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: NotificationConfirmationView,
    navigator: Navigator,
    objectStoreClient: PlayObjectStoreClient
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(notificationReference: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      objectStoreClient
        .listObjects(
          path = Path.Directory(s"/$objectStoreOwner/$notificationReference/"),
          owner = objectStoreOwner
        )
        .map { objectListing =>
          objectListing.objectSummaries match {
            case Nil => Ok(view(notificationReference, displayLink = false))
            case _   => Ok(view(notificationReference, displayLink = true))
          }
        }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      Redirect(navigator.nextPage(NotificationConfirmationPage, NormalMode, request.userAnswers))
    }
}

object NotificationConfirmationController {
  val objectStoreOwner = "senior-accounting-officer"
}
