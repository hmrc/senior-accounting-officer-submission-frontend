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

import controllers.notification.routes as notificationRoutes
import controllers.routes
import models.*
import models.notification.NotificationIdReferenceNumber
import models.upload.UploadTemplateTableData
import pages.*
import pages.notification.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case NotificationAdditionalInformationPage =>
      _ => notificationRoutes.ConfirmYourNotificationController.onPageLoad()
    case ConfirmYourNotificationPage =>
      _ => notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
    case NotificationCheckYourAnswersPage =>
      _ => notificationRoutes.NotificationConfirmationController.onPageLoad(notificationIdReferenceNumber.id)
    case IsThisTheSaoOnCertificatePage =>
      userAnswers =>
        userAnswers.get(IsThisTheSaoOnCertificatePage) match {
          case Some(true)  => routes.SaoEmailController.onPageLoad(NormalMode)
          case Some(false) => routes.SaoNameController.onPageLoad(NormalMode)
          case _           => ???
        }
    case SaoNamePage =>
      _ => routes.SaoEmailController.onPageLoad(NormalMode)
    case SaoEmailPage =>
      _ => routes.SaoEmailCommunicationChoiceController.onPageLoad(NormalMode)
    case SaoEmailCommunicationChoicePage =>
      _ => routes.CombinedCertificateCheckYourAnswersController.onPageLoad()
    case CombinedCertificateCheckYourAnswersPage =>
      _ => routes.CombinedWhoSubmitsCertificateController.onPageLoad(NormalMode)
    case CombinedWhoSubmitsCertificatePage =>
      _ => routes.QualifiedCompaniesController.onPageLoad()
    case QualifiedCompaniesPage =>
      _ => routes.UnqualifiedCompaniesController.onPageLoad()
    case UnqualifiedCompaniesPage =>
      _ => routes.CombinedCertificateDeclarationSaoController.onPageLoad(NormalMode)
    case CombinedCertificateDeclarationSaoPage =>
      _ => routes.CombinedCertificateConfirmationController.onPageLoad()
    case NotificationConfirmationPage =>
      _ => notificationRoutes.NotificationTaskListController.onComplete()
    case NotificationMoreThanOneSaoPage =>
      userAnswers =>
        userAnswers.get(NotificationMoreThanOneSaoPage) match {
          case Some(true)  => notificationRoutes.NotificationMultiSaoLastOfficerNameController.onPageLoad(NormalMode)
          case Some(false) => notificationRoutes.NotificationSingleSaoOfficerNameController.onPageLoad(NormalMode)
          case _           => ???
        }
    case NotificationSingleSaoOfficerNamePage =>
      _ => notificationRoutes.NotificationTaskListController.onPageLoad()
    case NotificationMultiSaoLastOfficerNamePage =>
      _ => notificationRoutes.NotificationMultiSaoLastOfficerStartDateController.onPageLoad(NormalMode)
    case NotificationMultiSaoLastOfficerStartDatePage =>
      _ => notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode)
    case NotificationMultiSaoPreviousOfficerNamePage(saoIndex) =>
      _ => notificationRoutes.NotificationMultiSaoPreviousOfficerStartDateController.onPageLoad(NormalMode, saoIndex)
    case NotificationMultiSaoPreviousOfficerStartDatePage(saoIndex) =>
      _ => notificationRoutes.NotificationMultiSaoPreviousOfficerEndDateController.onPageLoad(NormalMode, saoIndex)
    case NotificationMultiSaoPreviousOfficerEndDatePage(saoIndex) =>
      _ => notificationRoutes.NotificationMultiSaoAreAllAddedController.onPageLoad(NormalMode, saoIndex)
    case NotificationMultiSaoAreAllAddedPage(saoIndex) =>
      userAnswers =>
        userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex)) match {
          case Some(true)  => notificationRoutes.NotificationTaskListController.onPageLoad()
          case Some(false) =>
            notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode, saoIndex + 1)
          case _ => ???
        }
    case UploadTemplateTablePage =>
      userAnswers =>
        userAnswers
          .get(UploadTemplateTablePage)
          .fold(routes.JourneyRecoveryController.onPageLoad()) {
            case UploadTemplateTableData(_, errors) if errors.nonEmpty =>
              notificationRoutes.NotificationUploadFormController.onPageLoad()
            case _ => notificationRoutes.NotificationTaskListController.onPageLoad()
          }
    case SubmissionTypePage =>
      userAnswers =>
        userAnswers.get(SubmissionTypePage) match {
          case Some(SubmissionType.Notification) => notificationRoutes.NotificationTaskListController.onPageLoad()
          case Some(SubmissionType.Certificate)  =>
            routes.CertificateTaskListController.onPageLoad(CertificateTaskListStage.ProvideSaoDetailsStage)
          case _ => ???
        }
    // certificate flow
    case CertificateSaoFullNamePage =>
      _ => routes.CertificateSaoEmailController.onPageLoad(NormalMode)
    case CertificateSaoEmailPage =>
      _ =>
        routes.CertificateTaskListController.onPageLoad(stage = CertificateTaskListStage.UploadSubmissionTemplateStage)
    case CertificateReviewQualifiedPage =>
      _ => routes.CertificateReviewUnqualifiedController.onPageLoad()
    case CertificateReviewUnqualifiedPage =>
      _ => routes.CertificateTaskListController.onPageLoad(stage = CertificateTaskListStage.SubmitCertificateStage)
    case CertificateAdditionalInformationPage =>
      _ => routes.CertificateWhoIsSubmittingController.onPageLoad(NormalMode)
    case CertificateWhoIsSubmittingPage =>
      userAnswers =>
        userAnswers.get(CertificateWhoIsSubmittingPage) match {
          case Some(CertificateWhoIsSubmitting.Sao) =>
            routes.CertificateDeclarationSaoController.onPageLoad(NormalMode)
          case Some(CertificateWhoIsSubmitting.StandIn) =>
            routes.CertificateDeclarationStandInController.onPageLoad(NormalMode)
          case _ => ???
        }
    case CertificateDeclarationSaoPage | CertificateDeclarationStandInPage =>
      _ => routes.CertificateCheckYourAnswersController.onPageLoad()
    case CertificateConfirmationPage =>
      _ => routes.CertificateTaskListController.onPageLoad(stage = CertificateTaskListStage.Complete)
    case _ =>
      _ => ???
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case NotificationAdditionalInformationPage =>
      _ => notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
    case NotificationSingleSaoOfficerNamePage =>
      _ => notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
    case SaoNamePage =>
      _ => routes.CombinedCertificateCheckYourAnswersController.onPageLoad()
    case SaoEmailPage =>
      _ => routes.CombinedCertificateCheckYourAnswersController.onPageLoad()
    case SaoEmailCommunicationChoicePage =>
      _ => routes.CombinedCertificateCheckYourAnswersController.onPageLoad()
    case IsThisTheSaoOnCertificatePage =>
      userAnswers =>
        userAnswers.get(IsThisTheSaoOnCertificatePage) match {
          case Some(true)  => routes.CombinedCertificateCheckYourAnswersController.onPageLoad()
          case Some(false) => routes.SaoNameController.onPageLoad(CheckMode)
          case _           => ???
        }
    case _ => _ => ???
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

  val notificationIdReferenceNumber: NotificationIdReferenceNumber = NotificationIdReferenceNumber("SAONOT0123456789")
}
