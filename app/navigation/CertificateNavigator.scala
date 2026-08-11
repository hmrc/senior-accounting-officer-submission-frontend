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
import controllers.routes
import models.*
import models.certificate.{CertificateTaskListStage, CertificateWhoIsSubmitting}
import models.upload.UploadTemplateTableData
import pages.*
import pages.certificate.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class CertificateNavigator @Inject() () extends Navigator {

  override protected val normalRoutes: Page => UserAnswers => Call = {
    case CertificateSaoFullNamePage =>
      _ => certificateRoutes.CertificateSaoEmailController.onPageLoad(NormalMode)
    case CertificateSaoEmailPage =>
      _ =>
        certificateRoutes.CertificateTaskListController.onPageLoad(stage =
          CertificateTaskListStage.UploadSubmissionTemplateStage
        )
    case CertificateUploadTemplateTableErrorPage =>
      userAnswers =>
        userAnswers
          .get(CertificateUploadTemplateTablePage)
          .fold(routes.JourneyRecoveryController.onPageLoad()) { _ =>
            certificateRoutes.CertificateUploadFormController.onPageLoad()
          }
    case CertificateReviewQualifiedPage =>
      _ => certificateRoutes.CertificateReviewUnqualifiedController.onPageLoad()
    case CertificateReviewUnqualifiedPage =>
      _ =>
        certificateRoutes.CertificateTaskListController.onPageLoad(stage =
          CertificateTaskListStage.SubmitCertificateStage
        )
    case CertificateAdditionalInformationPage =>
      _ => certificateRoutes.CertificateWhoIsSubmittingController.onPageLoad(NormalMode)
    case CertificateWhoIsSubmittingPage =>
      userAnswers =>
        userAnswers.get(CertificateWhoIsSubmittingPage) match {
          case Some(CertificateWhoIsSubmitting.Sao) =>
            certificateRoutes.CertificateDeclarationSaoController.onPageLoad(NormalMode)
          case Some(CertificateWhoIsSubmitting.StandIn) =>
            certificateRoutes.CertificateDeclarationStandInController.onPageLoad(NormalMode)
          case _ => ???
        }
    case CertificateDeclarationSaoPage | CertificateDeclarationStandInPage =>
      _ => certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
    case CertificateConfirmationPage =>
      _ => certificateRoutes.CertificateTaskListController.onPageLoad(stage = CertificateTaskListStage.Complete)
    case _ =>
      _ => ???
  }

  override protected val checkRouteMap: Page => UserAnswers => Call = {
    case CertificateAdditionalInformationPage =>
      _ => certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
    case _ => _ => ???
  }

}
