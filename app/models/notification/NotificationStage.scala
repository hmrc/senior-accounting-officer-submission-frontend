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

import models.TaskStatus.{CannotStartYet, Completed, NotStarted}
import models.{TaskStatus, UserAnswers}
import pages.*
import pages.notification.*
import play.api.libs.json.*

enum NotificationStage(
    val provideSaoDetailsStatus: TaskStatus = NotStarted,
    val uploadNotificationTemplateStatus: TaskStatus = CannotStartYet,
    val submitNotificationStatus: TaskStatus = CannotStartYet
) {
  case ProvideSaoDetails extends NotificationStage()
  case UploadSubmissionTemplateDetails
      extends NotificationStage(
        provideSaoDetailsStatus = Completed,
        uploadNotificationTemplateStatus = NotStarted,
        submitNotificationStatus = CannotStartYet
      )
  case SubmitNotificationInfo
      extends NotificationStage(
        provideSaoDetailsStatus = Completed,
        uploadNotificationTemplateStatus = Completed,
        submitNotificationStatus = NotStarted
      )

  case AllStagesCompleted
      extends NotificationStage(
        provideSaoDetailsStatus = Completed,
        uploadNotificationTemplateStatus = Completed,
        submitNotificationStatus = Completed
      )
}

object NotificationStage {

  def taskListStage(userAnswers: UserAnswers): NotificationStage =
    if !isProvideSaoDetailsComplete(userAnswers) then {
      ProvideSaoDetails
    } else if !isUploadNotificationTemplateComplete(userAnswers) then {
      UploadSubmissionTemplateDetails
    } else {
      SubmitNotificationInfo
    }

  def canStartUploadNotificationTemplate(userAnswers: UserAnswers): Boolean =
    isProvideSaoDetailsComplete(userAnswers)

  def canStartSubmitNotification(userAnswers: UserAnswers): Boolean =
    isProvideSaoDetailsComplete(userAnswers) && isUploadNotificationTemplateComplete(userAnswers)

  private def isProvideSaoDetailsComplete(userAnswers: UserAnswers): Boolean =
    userAnswers.get(NotificationMoreThanOneSaoPage).exists {
      case false =>
        userAnswers.get(OneSaoSubmitNotificationFullNamePage).exists(_.trim.nonEmpty)
      case true =>
        userAnswers.get(NotificationMultiSaoLastOfficerNamePage).exists(_.trim.nonEmpty) &&
        hasCompletedMoreSaoDetails(userAnswers)
    }

  private def hasCompletedMoreSaoDetails(userAnswers: UserAnswers): Boolean =
    (userAnswers.data \ NotificationMoreSaoAreAllAddedPage(0).key)
      .asOpt[Seq[Boolean]]
      .exists(_.contains(true))

  private def isUploadNotificationTemplateComplete(userAnswers: UserAnswers): Boolean =
    userAnswers.get(UploadTemplateTablePage).exists(_.errors.isEmpty)
}
