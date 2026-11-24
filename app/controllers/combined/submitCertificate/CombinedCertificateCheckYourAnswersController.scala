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

package controllers.combined.submitCertificate

import config.AppConfig
import controllers.actions.*
import controllers.combined.submitCertificate.routes as submitCertificateRoutes
import models.JourneySection
import models.JourneySection.Combined
import pages.certificate.CertificateSubmissionTokenPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import services.*
import services.CertificateSubmissionService.CertificateSubmissionResult
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.combined.submitCertificate.CombinedCertificateCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

import java.util.UUID
import javax.inject.Inject

class CombinedCertificateCheckYourAnswersController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    combinedJourneyCheck: CombinedJourneyAction,
    val controllerComponents: MessagesControllerComponents,
    sessionRepository: SessionRepository,
    certificateCheckYourAnswersService: CertificateCheckYourAnswersService,
    certificateSubmissionService: CertificateSubmissionService,
    declarationUserAnswersService: DeclarationUserAnswersService,
    view: CombinedCertificateCheckYourAnswersView,
    appConfig: AppConfig,
    requireSubmitCertificateStageUnlocked: RequireCombinedSubmitCertificateUnlockedAction
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen combinedJourneyCheck andThen requireSubmitCertificateStageUnlocked)
      .async { implicit request =>
        val sanitisedAnswers = declarationUserAnswersService.sanitise(request.userAnswers)
        val summaryList      = certificateCheckYourAnswersService.getSummaryList(sanitisedAnswers)
        val token            = UUID.randomUUID().toString

        for {
          updatedAnswers <- Future.fromTry(sanitisedAnswers.set(CertificateSubmissionTokenPage, token))
          _              <- sessionRepository.set(updatedAnswers)
        } yield Ok(view(summaryList, token))
      }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen combinedJourneyCheck andThen requireSubmitCertificateStageUnlocked)
      .async { implicit request =>
        given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        request.body.asFormUrlEncoded.flatMap(submissionToken) match {
          case None =>
            Future.successful(
              Redirect(
                controllers.routes.JourneyRecoveryController.onPageLoad(section = Some(JourneySection.Combined))
              )
            )
          case Some(token) =>
            val submission =
              if appConfig.faultToleranceEnabled then
                certificateSubmissionService.submitWithFaultTolerance(
                  request.userId,
                  request.userAnswers,
                  token,
                  isCombinedJourney = true
                )
              else
                certificateSubmissionService.submit(
                  request.userId,
                  request.userAnswers,
                  token,
                  isCombinedJourney = true
                )

            submission.map {
              case CertificateSubmissionResult.Submitted(certificateRef) =>
                Redirect(submitCertificateRoutes.CombinedCertificateConfirmationController.onPageLoad(certificateRef))
              case CertificateSubmissionResult.Pending(idempotencyKey) =>
                Redirect(submitCertificateRoutes.CombinedCertificateSubmittingController.onPageLoad(idempotencyKey))
              case CertificateSubmissionResult.Duplicate =>
                Redirect(submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onPageLoad())
              case CertificateSubmissionResult.MissingData =>
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad(section = Some(Combined)))
              case CertificateSubmissionResult.Failed =>
                throw CombinedCertificateCheckYourAnswersController.SubmissionFailedException
            }
        }
      }

  private def submissionToken(form: Map[String, Seq[String]]): Option[String] =
    form.collectFirst { case (CombinedCertificateCheckYourAnswersController.TokenField, token +: _) =>
      token
    }
}

object CombinedCertificateCheckYourAnswersController {
  val TokenField: String = "certificateSubmissionToken"

  object SubmissionFailedException extends RuntimeException("Certificate submission failed")
}
