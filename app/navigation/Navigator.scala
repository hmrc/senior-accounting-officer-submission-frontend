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

import controllers.routes
import models.*
import models.upload.UploadTemplateTableData
import pages.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case NotificationAdditionalInformationPage =>
      _ => routes.ConfirmYourNotificationController.onPageLoad()
    case ConfirmYourNotificationPage =>
      _ => routes.NotificationCheckYourAnswersController.onPageLoad()
    case NotificationCheckYourAnswersPage =>
      _ => routes.NotificationConfirmationController.onPageLoad(notificationIdReferenceNumber.id)
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
      _ => routes.SubmitNotificationStartController.onComplete()
    case NotificationMoreThanOneSaoPage =>
      userAnswers =>
        userAnswers.get(NotificationMoreThanOneSaoPage) match {
          case Some(true)  => routes.MoreSaoSubmitNotificationFullNameController.onPageLoad(NormalMode)
          case Some(false) => routes.OneSaoSubmitNotificationFullNameController.onPageLoad(NormalMode)
          case _           => ???
        }
    case OneSaoSubmitNotificationFullNamePage =>
      _ => routes.SubmitNotificationStartController.onPageLoad()
    case MoreSaoSubmitNotificationFullNamePage =>
      _ => routes.NotificationMoreSaoFirstStartDateController.onPageLoad(NormalMode)
    case NotificationMoreSaoFirstStartDatePage =>
      _ => routes.WhoWasTheSaoBeforeController.onPageLoad(NormalMode)
    case WhoWasTheSaoBeforePage(saoIndex) =>
      _ => routes.NotificationMoreSaoSecondStartDateController.onPageLoad(NormalMode, saoIndex)
    case NotificationMoreSaoSecondStartDatePage(saoIndex) =>
      _ => routes.NotificationMoreSaoSecondEndDateController.onPageLoad(NormalMode, saoIndex)
    case NotificationMoreSaoSecondEndDatePage(saoIndex) =>
      _ => routes.NotificationMoreSaoAreAllAddedController.onPageLoad(NormalMode, saoIndex)
    case NotificationMoreSaoAreAllAddedPage(saoIndex) =>
      userAnswers =>
        userAnswers.get(NotificationMoreSaoAreAllAddedPage(saoIndex)) match {
          case Some(true)  => routes.SubmitNotificationStartController.onPageLoad()
          case Some(false) => routes.WhoWasTheSaoBeforeController.onPageLoad(NormalMode, saoIndex + 1)
          case _           => ???
        }
    case UploadTemplateTablePage =>
      userAnswers =>
        userAnswers
          .get(UploadTemplateTablePage)
          .fold(routes.JourneyRecoveryController.onPageLoad()) {
            case UploadTemplateTableData(_, errors) if errors.nonEmpty =>
              routes.NotificationUploadFormController.onPageLoad()
            case _ => routes.SubmitNotificationStartController.onPageLoad()
          }
    case SubmissionTypePage =>
      userAnswers =>
        userAnswers.get(SubmissionTypePage) match {
          case Some(SubmissionType.Notification) => routes.SubmitNotificationStartController.onPageLoad()
          case Some(SubmissionType.Certificate)  => routes.CertificateTaskListController.onPageLoad()
          case _                                 => ???
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
    case CertificateCheckYourAnswersPage =>
      _ => routes.CertificateConfirmationController.onPageLoad()
    case CertificateConfirmationPage =>
      _ => routes.CertificateTaskListController.onPageLoad(stage = CertificateTaskListStage.Complete)
    case _ =>
      _ => ???
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case NotificationAdditionalInformationPage =>
      _ => routes.NotificationCheckYourAnswersController.onPageLoad()
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
