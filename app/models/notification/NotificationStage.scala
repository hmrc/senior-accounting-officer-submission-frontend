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

package models.notification

import models.Area.COMMITTED_PATH
import models.NormalMode
import models.{TaskStatus, UserAnswers}
import pages.*
import pages.Page.NOTIFICATION_PATH
import pages.notification.*
import play.api.libs.json.*
import play.api.mvc.JavascriptLiteral

enum NotificationStage {
    case ProvideSaoDetailsStage, UploadNotificationTemplateStage, SubmitNotificationStage, Complete
}

object NotificationStage {

  given jsLiteral: JavascriptLiteral[NotificationStage] = new JavascriptLiteral[NotificationStage] {
    override def to(value: NotificationStage): String = value match {
      case ProvideSaoDetailsStage => "ProvideSaoDetailsStage"
      case UploadNotificationTemplateStage => "UploadNotificationTemplateStage"
      case SubmitNotificationStage => "SubmitNotificationStage"
      case Complete => "Complete"
    }
  }


  extension (stage: NotificationStage) {
    def toState(): NotificationTaskListState = {
      stage match {
        case NotificationStage.ProvideSaoDetailsStage =>
          NotificationTaskListState(
            provideSaoDetailsStage = TaskStatus.NotStarted,
            uploadNotificationTemplateStage = TaskStatus.CannotStartYet,
            submitNotificationStage = TaskStatus.CannotStartYet,
            showViewYourConfirmationButton = false
          )
        case NotificationStage.UploadNotificationTemplateStage =>
          NotificationTaskListState(
            provideSaoDetailsStage = TaskStatus.Completed,
            uploadNotificationTemplateStage = TaskStatus.NotStarted,
            submitNotificationStage = TaskStatus.CannotStartYet,
            showViewYourConfirmationButton = false
          )
        case NotificationStage.SubmitNotificationStage =>
          NotificationTaskListState(
            provideSaoDetailsStage = TaskStatus.Completed,
            uploadNotificationTemplateStage = TaskStatus.Completed,
            submitNotificationStage = TaskStatus.NotStarted,
            showViewYourConfirmationButton = false
          )
        case NotificationStage.Complete =>
          NotificationTaskListState(
            provideSaoDetailsStage = TaskStatus.Completed,
            uploadNotificationTemplateStage = TaskStatus.Completed,
            submitNotificationStage = TaskStatus.Completed,
            showViewYourConfirmationButton = true
          )
      }
    }
  }

  def canStartUploadNotificationTemplate(userAnswers: UserAnswers): Boolean =
    isProvideSaoDetailsComplete(userAnswers)

  def canStartSubmitNotification(userAnswers: UserAnswers): Boolean =
    isProvideSaoDetailsComplete(userAnswers) && isUploadNotificationTemplateComplete(userAnswers)

  private def isProvideSaoDetailsComplete(userAnswers: UserAnswers): Boolean = {
    val singleSaoCompleted =
      (for
        isMoreThanOneSao <- userAnswers.get(NotificationMoreThanOneSaoPage(NormalMode))
        _                <- userAnswers.get(NotificationSingleSaoOfficerNamePage(NormalMode))
      yield !isMoreThanOneSao).getOrElse(false)

    val multiSaoCompleted = userAnswers.get(NotificationMoreThanOneSaoPage(NormalMode)).contains(true) &&
      (userAnswers.data \ NOTIFICATION_PATH \ COMMITTED_PATH \ NotificationMultiSaoAreAllAddedPage(0, NormalMode).key)
        .asOpt[Seq[Boolean]]
        .exists(_.contains(true))

    singleSaoCompleted || multiSaoCompleted
  }

  private def isUploadNotificationTemplateComplete(userAnswers: UserAnswers): Boolean =
    userAnswers.get(UploadTemplateTablePage).exists(_.errors.isEmpty) &&
      userAnswers.get(UploadTemplateReviewPage).contains(true)
}
