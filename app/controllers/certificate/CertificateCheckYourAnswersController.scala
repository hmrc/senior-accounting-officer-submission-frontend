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
import controllers.certificate.routes as certificateRoutes
import pages.certificate.CertificateSubmissionTokenPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import services.CertificateCheckYourAnswersService
import services.CertificateSubmissionService
import services.CertificateSubmissionService.CertificateSubmissionResult
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.certificate.CertificateCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

import java.util.UUID
import javax.inject.Inject

class CertificateCheckYourAnswersController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    sessionRepository: SessionRepository,
    certificateCheckYourAnswersService: CertificateCheckYourAnswersService,
    certificateSubmissionService: CertificateSubmissionService,
    view: CertificateCheckYourAnswersView
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val summaryList = certificateCheckYourAnswersService.getSummaryList(request.userAnswers)
    val token       = UUID.randomUUID().toString

    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(CertificateSubmissionTokenPage, token))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Ok(view(summaryList, token))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    request.body.asFormUrlEncoded.flatMap(submissionToken) match {
      case None =>
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case Some(token) =>
        certificateSubmissionService
          .submit(request.userId, request.saoSubscriptionId, request.userAnswers, token)
          .flatMap {
            case CertificateSubmissionResult.Submitted(certificateRef) =>
              Future.successful(
                Redirect(certificateRoutes.CertificateConfirmationController.onPageLoad(certificateRef))
              )
            case CertificateSubmissionResult.Duplicate =>
              Future.successful(Redirect(certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()))
            case CertificateSubmissionResult.MissingData =>
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            case CertificateSubmissionResult.Failed =>
              Future.failed(CertificateCheckYourAnswersController.SubmissionFailedException)
          }
    }
  }

  private def submissionToken(form: Map[String, Seq[String]]): Option[String] =
    form.collectFirst { case (CertificateCheckYourAnswersController.TokenField, token +: _) =>
      token
    }
}

object CertificateCheckYourAnswersController {
  val TokenField: String = "certificateSubmissionToken"

  object SubmissionFailedException extends RuntimeException("Certificate submission failed")
}
