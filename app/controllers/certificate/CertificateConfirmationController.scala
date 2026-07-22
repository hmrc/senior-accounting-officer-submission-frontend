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

package controllers.certificate

import controllers.actions.*
import models.NormalMode
import navigation.Navigator
import pages.certificate.CertificateConfirmationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ObjectStoreService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.certificate.CertificateConfirmationView

import scala.concurrent.ExecutionContext

import javax.inject.Inject

class CertificateConfirmationController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: CertificateConfirmationView,
    navigator: Navigator,
    // objectStoreClient: PlayObjectStoreClient
    objectStoreService: ObjectStoreService
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(certificateReference: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      // objectStoreClient
      // .listObjects(
      // path = Path.Directory(s"/$objectStoreOwner/$certificateReference/"),
      // owner = objectStoreOwner
      // )
      // .map { objectListing =>
      // objectListing.objectSummaries match {
      // case Nil => Ok(view(certificateReference, displayLink = false))
      // case _   => Ok(view(certificateReference, displayLink = true))
      // }
      // }
      objectStoreService.isCertificatePdfAvailable(certificateReference).map { isPdfAvailable =>
        Ok(
          view(
            certificateReference = certificateReference,
            displayPdfLink = isPdfAvailable
          )
        )
      }
    }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    Redirect(navigator.nextPage(CertificateConfirmationPage, NormalMode, request.userAnswers))
  }
}

// object CertificateConfirmationController {
// val objectStoreOwner = "senior-accounting-officer"
// }
