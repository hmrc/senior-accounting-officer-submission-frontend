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

import controllers.combined.provideSaoDetails.routes as provideSaoDetailsRoutes
import controllers.combined.routes as combinedRoutes
import controllers.combined.submitNotification.routes as submitNotificationRoutes
import controllers.combined.uploadTemplate.routes as uploadTemplateRoutes
import controllers.routes
import models.*
import models.upload.UploadTemplateTableData
import pages.*
import pages.Page.NOTIFICATION_PATH
import pages.combined.CombinedNotificationConfirmationPage
import pages.notification.*
import play.api.mvc.Call
import models.upload.UploadTemplateTableData

import javax.inject.{Inject, Singleton}

@Singleton
class CombinedNavigator @Inject() () extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)
      case TransactionMode => transactionRouteMap(page)(userAnswers)
    }

  override protected val normalRoutes: Page => UserAnswers => Call = {
    case NotificationMoreThanOneSaoPage(NormalMode) =>
      userAnswers =>
        userAnswers.get(NotificationMoreThanOneSaoPage(NormalMode)) match {
          case Some(true)  => provideSaoDetailsRoutes.CombinedMultiSaoLastOfficerNameController.onPageLoad(NormalMode)
          case Some(false) => provideSaoDetailsRoutes.CombinedSingleSaoOfficerNameController.onPageLoad(NormalMode)
          case answer => throw new IllegalStateException(s"Unexpected NotificationMoreThanOneSaoPage value - $answer")
        }
    case NotificationSingleSaoOfficerNamePage(mode) =>
      _ => combinedRoutes.CombinedTaskListController.onPageLoad
    case NotificationMultiSaoLastOfficerNamePage(mode) =>
      _ => provideSaoDetailsRoutes.CombinedMultiSaoLastOfficerStartDateController.onPageLoad(NormalMode)
    case NotificationMultiSaoLastOfficerStartDatePage(mode) =>
      _ => provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode)
    case NotificationMultiSaoPreviousOfficerNamePage(saoIndex, mode) =>
      _ => provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerStartDateController.onPageLoad(NormalMode, saoIndex)
    case NotificationMultiSaoPreviousOfficerStartDatePage(saoIndex, mode) =>
      _ => provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerEndDateController.onPageLoad(NormalMode, saoIndex)
    case NotificationMultiSaoPreviousOfficerEndDatePage(saoIndex, mode) =>
      _ => provideSaoDetailsRoutes.CombinedMultiSaoAreAllAddedController.onPageLoad(NormalMode, saoIndex)
    case NotificationMultiSaoAreAllAddedPage(saoIndex, mode) =>
      userAnswers =>
        userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex, mode)) match {
          case Some(true)  => combinedRoutes.CombinedTaskListController.onPageLoad
          case Some(false) =>
            provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode, saoIndex + 1)
          case answer =>
            throw new IllegalStateException(s"Unexpected NotificationMultiSaoAreAllAddedPage value - $answer")
        }

    case UploadTemplateTablePage =>
      userAnswers =>
        userAnswers
          .get(UploadTemplateTablePage)
          .fold(routes.JourneyRecoveryController.onPageLoad()) {
            case UploadTemplateTableData(_, errors) if errors.nonEmpty =>
              uploadTemplateRoutes.CombinedUploadFormController.onPageLoad()
            case _ => combinedRoutes.CombinedTaskListController.onPageLoad
          }

    case NotificationAdditionalInformationPage =>
      _ => submitNotificationRoutes.CombinedConfirmYourNotificationController.onPageLoad()
    case ConfirmYourNotificationPage =>
      _ => submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
    case CombinedNotificationConfirmationPage =>
      _ => combinedRoutes.CombinedTaskListController.onPageLoad

//    case CertificateSaoFullNamePage =>
//      _ => submitCertificateRoutes.CombinedCertificateSaoEmailController.onPageLoad(NormalMode)
//    case CertificateSaoEmailPage =>
//      _ => submitCertificateRoutes.CombinedCertificateReviewQualifiedController.onPageLoad(page = 0)
//    case CertificateReviewQualifiedPage =>
//      _ => submitCertificateRoutes.CombinedCertificateReviewUnqualifiedController.onPageLoad(page = 0)
//    case CertificateReviewUnqualifiedPage =>
//      _ => submitCertificateRoutes.CombinedCertificateAdditionalInformationController.onPageLoad(NormalMode)
//    case CertificateAdditionalInformationPage =>
//      _ => submitCertificateRoutes.CombinedCertificateWhoIsSubmittingController.onPageLoad(NormalMode)
//    case CertificateWhoIsSubmittingPage(_) =>
//      userAnswers =>
//        userAnswers.get(CertificateWhoIsSubmittingPage(NormalMode)) match {
//          case Some(Sao) =>
//            submitCertificateRoutes.CombinedCertificateDeclarationSaoController.onPageLoad(NormalMode)
//          case Some(StandIn) =>
//            submitCertificateRoutes.CombinedCertificateDeclarationStandInController.onPageLoad(NormalMode)
//          case answer => throw new IllegalStateException(s"Unexpected CertificateWhoIsSubmittingPage value - $answer")
//        }
//    case CertificateDeclarationSaoPage(_) | CertificateDeclarationStandInPage(_) =>
//      _ => submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onPageLoad()
//    case CombinedCertificateConfirmationPage =>
//      _ => combinedRoutes.CombinedTaskListController.onComplete

    case page => _ => throw new IllegalStateException(s"Unexpected Page in normalRoutes - $page")
  }

  override protected val checkRouteMap: Page => UserAnswers => Call = {
    case NotificationSingleSaoOfficerNamePage(mode) =>
      _ => submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
    case NotificationMultiSaoLastOfficerNamePage(mode) =>
      _ => submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
    case NotificationMultiSaoLastOfficerStartDatePage(mode) =>
      _ => submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
    case NotificationMultiSaoPreviousOfficerNamePage(_, mode) =>
      _ => submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
    case NotificationMultiSaoPreviousOfficerStartDatePage(_, mode) =>
      _ => submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
    case NotificationMultiSaoPreviousOfficerEndDatePage(_, mode) =>
      _ => submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
    case NotificationMultiSaoAreAllAddedPage(saoIndex, mode) =>
      userAnswers =>
        if hasCompletedMoreSaoDetails(userAnswers) then {
          submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
        } else {
          provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode, saoIndex + 1)
        }
    case NotificationAdditionalInformationPage =>
      _ => submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
    case page => _ => throw new IllegalStateException(s"Unexpected Page in checkRouteMap - $page")
  }

  private def hasSingleSaoAnswers(userAnswers: UserAnswers, mode: Mode): Boolean = {
    userAnswers.get(NotificationMoreThanOneSaoPage(mode)).nonEmpty &&
    userAnswers.get(NotificationSingleSaoOfficerNamePage(mode)).nonEmpty
  }

  private def hasMultiSaoAnswers(userAnswers: UserAnswers, mode: Mode): Boolean = {
    userAnswers.get(NotificationMultiSaoLastOfficerNamePage(mode)).nonEmpty &&
    userAnswers.get(NotificationMultiSaoLastOfficerStartDatePage(mode)).nonEmpty &&
    userAnswers.get(NotificationMultiSaoPreviousOfficerNamePage(0, mode)).nonEmpty &&
    userAnswers.get(NotificationMultiSaoPreviousOfficerStartDatePage(0, mode)).nonEmpty &&
    userAnswers.get(NotificationMultiSaoPreviousOfficerEndDatePage(0, mode)).nonEmpty &&
    userAnswers.get(NotificationMultiSaoAreAllAddedPage(0, mode)).nonEmpty
  }

  private def hasCompletedMoreSaoDetails(userAnswers: UserAnswers): Boolean = {
    (userAnswers.data \ NOTIFICATION_PATH \ NotificationMultiSaoAreAllAddedPage(0, NormalMode).key)
      .asOpt[Seq[Boolean]]
      .exists(_.contains(true))
  }

  val transactionRouteMap: Page => UserAnswers => Call = {
    case NotificationMoreThanOneSaoPage(_) =>
      userAnswers =>
        userAnswers.get(NotificationMoreThanOneSaoPage(TransactionMode)) match {
          case Some(true) => {
            if hasMultiSaoAnswers(userAnswers, NormalMode) then {
              submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
            } else {
              provideSaoDetailsRoutes.CombinedMultiSaoLastOfficerNameController.onPageLoad(TransactionMode)
            }
          }
          case Some(false) => {
            if hasSingleSaoAnswers(userAnswers, NormalMode) then {
              submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
            } else {
              provideSaoDetailsRoutes.CombinedSingleSaoOfficerNameController.onPageLoad(TransactionMode)
            }
          }
          case None => ???
        }
    case NotificationSingleSaoOfficerNamePage(_) =>
      _ => submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
    case NotificationMultiSaoLastOfficerNamePage(_) =>
      _ => provideSaoDetailsRoutes.CombinedMultiSaoLastOfficerStartDateController.onPageLoad(TransactionMode)
    case NotificationMultiSaoLastOfficerStartDatePage(_) =>
      _ => provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerNameController.onPageLoad(TransactionMode)
    case NotificationMultiSaoAreAllAddedPage(saoIndex, _) =>
      userAnswers =>
        userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex, TransactionMode)) match {
          case Some(true)  => submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
          case Some(false) =>
            userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex, NormalMode)) match {
              case Some(true) | None =>
                provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerNameController.onPageLoad(
                  TransactionMode,
                  saoIndex + 1
                )
              case Some(false) => submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
            }
          case answer =>
            throw new IllegalStateException(s"Unexpected NotificationMultiSaoAreAllAddedPage value - $answer")
        }
    case NotificationMultiSaoPreviousOfficerNamePage(saoIndex, _) =>
      _ =>
        provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerStartDateController.onPageLoad(TransactionMode, saoIndex)
    case NotificationMultiSaoPreviousOfficerStartDatePage(saoIndex, _) =>
      _ =>
        provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerEndDateController.onPageLoad(TransactionMode, saoIndex)
    case NotificationMultiSaoPreviousOfficerEndDatePage(saoIndex, _) =>
      _ => provideSaoDetailsRoutes.CombinedMultiSaoAreAllAddedController.onPageLoad(TransactionMode, saoIndex)
    case page => _ => throw new IllegalStateException(s"Unexpected page - $page")
  }
}
