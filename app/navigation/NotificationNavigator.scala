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
import models.upload.UploadTemplateTableData
import pages.*
import pages.Page.NOTIFICATION_PATH
import pages.notification.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class NotificationNavigator @Inject() () extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)
      case TransactionMode => transactionRouteMap(page)(userAnswers)
    }

  override protected val normalRoutes: Page => UserAnswers => Call = {
    case NotificationAdditionalInformationPage =>
      _ => notificationRoutes.ConfirmYourNotificationController.onPageLoad()
    case ConfirmYourNotificationPage =>
      _ => notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
    case NotificationConfirmationPage =>
      _ => notificationRoutes.NotificationTaskListController.onComplete()
    case NotificationMoreThanOneSaoPage(NormalMode) =>
      userAnswers =>
        userAnswers.get(NotificationMoreThanOneSaoPage(NormalMode)) match {
          case Some(true)  => notificationRoutes.NotificationMultiSaoLastOfficerNameController.onPageLoad(NormalMode)
          case Some(false) => notificationRoutes.NotificationSingleSaoOfficerNameController.onPageLoad(NormalMode)
          case _           => ???
        }
    case NotificationSingleSaoOfficerNamePage(mode) =>
      _ => notificationRoutes.NotificationTaskListController.onPageLoad()
    case NotificationMultiSaoLastOfficerNamePage(mode) =>
      _ => notificationRoutes.NotificationMultiSaoLastOfficerStartDateController.onPageLoad(NormalMode)
    case NotificationMultiSaoLastOfficerStartDatePage(mode) =>
      _ => notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode)
    case NotificationMultiSaoPreviousOfficerNamePage(saoIndex, mode) =>
      _ => notificationRoutes.NotificationMultiSaoPreviousOfficerStartDateController.onPageLoad(NormalMode, saoIndex)
    case NotificationMultiSaoPreviousOfficerStartDatePage(saoIndex, mode) =>
      _ => notificationRoutes.NotificationMultiSaoPreviousOfficerEndDateController.onPageLoad(NormalMode, saoIndex)
    case NotificationMultiSaoPreviousOfficerEndDatePage(saoIndex, mode) =>
      _ => notificationRoutes.NotificationMultiSaoAreAllAddedController.onPageLoad(NormalMode, saoIndex)
    case NotificationMultiSaoAreAllAddedPage(saoIndex, mode) =>
      userAnswers =>
        userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex, mode)) match {
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
    case _ => _ => ???
  }

  override protected val checkRouteMap: Page => UserAnswers => Call = {
    case NotificationSingleSaoOfficerNamePage(mode) =>
      _ => notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
    case NotificationMultiSaoLastOfficerNamePage(mode) =>
      _ => notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
    case NotificationMultiSaoLastOfficerStartDatePage(mode) =>
      _ => notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
    case NotificationMultiSaoPreviousOfficerNamePage(_, mode) =>
      _ => notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
    case NotificationMultiSaoPreviousOfficerStartDatePage(_, mode) =>
      _ => notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
    case NotificationMultiSaoPreviousOfficerEndDatePage(_, mode) =>
      _ => notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
    case NotificationMultiSaoAreAllAddedPage(saoIndex, mode) =>
      userAnswers =>
        if hasCompletedMoreSaoDetails(userAnswers) then {
          notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
        } else {
          notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode, saoIndex + 1)
        }
    case NotificationAdditionalInformationPage =>
      _ => notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
    case _ => _ => ???
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
              notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
            } else {
              notificationRoutes.NotificationMultiSaoLastOfficerNameController.onPageLoad(TransactionMode)
            }
          }
          case Some(false) => {
            if hasSingleSaoAnswers(userAnswers, NormalMode) then {
              notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
            } else {
              notificationRoutes.NotificationSingleSaoOfficerNameController.onPageLoad(TransactionMode)
            }
          }
          case _ => ???
        }
    case NotificationSingleSaoOfficerNamePage(_) =>
      _ => notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
    case NotificationMultiSaoLastOfficerNamePage(_) =>
      _ => notificationRoutes.NotificationMultiSaoLastOfficerStartDateController.onPageLoad(TransactionMode)
    case NotificationMultiSaoLastOfficerStartDatePage(_) =>
      _ => notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(TransactionMode)
    case NotificationMultiSaoAreAllAddedPage(saoIndex, _) =>
      userAnswers =>
        userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex, TransactionMode)) match {
          case Some(true)  => notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
          case Some(false) =>
            notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(
              TransactionMode,
              saoIndex + 1
            )
          case _ => ???
        }
    case NotificationMultiSaoPreviousOfficerNamePage(saoIndex, _) =>
      _ =>
        notificationRoutes.NotificationMultiSaoPreviousOfficerStartDateController.onPageLoad(TransactionMode, saoIndex)
    case NotificationMultiSaoPreviousOfficerStartDatePage(saoIndex, _) =>
      _ => notificationRoutes.NotificationMultiSaoPreviousOfficerEndDateController.onPageLoad(TransactionMode, saoIndex)
    case NotificationMultiSaoPreviousOfficerEndDatePage(saoIndex, _) =>
      _ => notificationRoutes.NotificationMultiSaoAreAllAddedController.onPageLoad(TransactionMode, saoIndex)
    case _ => _ => ???
  }
}
