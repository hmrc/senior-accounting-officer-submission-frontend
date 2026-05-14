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

package models

import models.SubmitNotificationStatus.{CannotStartYet, Completed, NotStarted}
import pages.*

enum SubmitNotificationStatus {
  case CannotStartYet, NotStarted, Completed
}

enum SubmitNotificationStage(
    val provideSaoDetailsStatus: SubmitNotificationStatus = NotStarted,
    val uploadNotificationTemplateStatus: SubmitNotificationStatus = CannotStartYet,
    val submitNotificationStatus: SubmitNotificationStatus = CannotStartYet
) {
  case ProvideSaoDetails extends SubmitNotificationStage()
  case UploadSubmissionTemplateDetails
      extends SubmitNotificationStage(
        provideSaoDetailsStatus = Completed,
        uploadNotificationTemplateStatus = NotStarted,
        submitNotificationStatus = CannotStartYet
      )
  case SubmitNotificationInfo
      extends SubmitNotificationStage(
        provideSaoDetailsStatus = Completed,
        uploadNotificationTemplateStatus = Completed,
        submitNotificationStatus = NotStarted
      )
}

object SubmitNotificationStage {

  def from(userAnswers: UserAnswers): SubmitNotificationStage =
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
        userAnswers.get(MoreSaoSubmitNotificationFullNamePage).exists(_.trim.nonEmpty) &&
        hasCompletedMoreSaoDetails(userAnswers)
    }

  private def hasCompletedMoreSaoDetails(userAnswers: UserAnswers): Boolean =
    (0 to 50).exists(index => userAnswers.get(NotificationMoreSaoAreAllAddedPage(index)).contains(true))

  private def isUploadNotificationTemplateComplete(userAnswers: UserAnswers): Boolean =
    userAnswers.get(UploadTemplateTablePage).exists(_.errors.isEmpty)
}
