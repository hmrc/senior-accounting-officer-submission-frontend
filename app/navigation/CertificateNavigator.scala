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

package navigation

import controllers.certificate.routes as certificateRoutes
import models.*
import models.certificate.CertificateTaskListStage
import models.certificate.CertificateWhoIsSubmitting.*
import pages.*
import pages.certificate.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class CertificateNavigator @Inject() () extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)
      case TransactionMode => transactionRouteMap(page)(userAnswers)
    }

  override protected val normalRoutes: Page => UserAnswers => Call = {
    case CertificateSaoFullNamePage =>
      _ => certificateRoutes.CertificateSaoEmailController.onPageLoad(NormalMode)
    case CertificateSaoEmailPage =>
      _ =>
        certificateRoutes.CertificateTaskListController.onPageLoad(stage =
          CertificateTaskListStage.UploadSubmissionTemplateStage
        )
    case CertificateReviewQualifiedPage =>
      _ => certificateRoutes.CertificateReviewUnqualifiedController.onPageLoad()
    case CertificateReviewUnqualifiedPage =>
      _ =>
        certificateRoutes.CertificateTaskListController.onPageLoad(stage =
          CertificateTaskListStage.SubmitCertificateStage
        )
    case CertificateAdditionalInformationPage =>
      _ => certificateRoutes.CertificateWhoIsSubmittingController.onPageLoad(NormalMode)
    case CertificateWhoIsSubmittingPage(_) =>
      userAnswers =>
        userAnswers.get(CertificateWhoIsSubmittingPage(NormalMode)) match {
          case Some(Sao) =>
            certificateRoutes.CertificateDeclarationSaoController.onPageLoad(NormalMode)
          case Some(StandIn) =>
            certificateRoutes.CertificateDeclarationStandInController.onPageLoad(NormalMode)
          case _ => ???
        }
    case CertificateDeclarationSaoPage(_) | CertificateDeclarationStandInPage(_) =>
      _ => certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
    case CertificateConfirmationPage =>
      _ => certificateRoutes.CertificateTaskListController.onPageLoad(stage = CertificateTaskListStage.Complete)
    case _ =>
      _ => ???
  }

  override protected val checkRouteMap: Page => UserAnswers => Call = {
    case CertificateSaoFullNamePage =>
      _ => certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
    case CertificateSaoEmailPage =>
      _ => certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
    case CertificateDeclarationSaoPage(_) =>
      _ => certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
    case CertificateDeclarationStandInPage(_) =>
      _ => certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
    case CertificateAdditionalInformationPage =>
      _ => certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
    case _ => _ => ???
  }

  protected val transactionRouteMap: Page => UserAnswers => Call = {
    case CertificateDeclarationSaoPage(TransactionMode) | CertificateDeclarationStandInPage(TransactionMode) =>
      _ => certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
    case CertificateWhoIsSubmittingPage(TransactionMode) =>
      userAnswers =>
        val committedAnswer   = userAnswers.get(CertificateWhoIsSubmittingPage(NormalMode))
        val transactionAnswer = userAnswers.get(CertificateWhoIsSubmittingPage(TransactionMode))
        (committedAnswer, transactionAnswer) match {
          case (Some(Sao), Some(Sao)) =>
            certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
          case (Some(Sao), Some(StandIn)) =>
            certificateRoutes.CertificateDeclarationStandInController.onPageLoad(TransactionMode)
          case (Some(StandIn), Some(Sao)) =>
            certificateRoutes.CertificateDeclarationSaoController.onPageLoad(TransactionMode)
          case (Some(StandIn), Some(StandIn)) =>
            certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
          case _ => ???
        }
    case _ => _ => ???
  }
}
