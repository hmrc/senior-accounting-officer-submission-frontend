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

package models.combined

import models.Area.COMMITTED_PATH
import models.TaskStatus.{CannotStartYet, Completed, NotStarted}
import models.{NormalMode, TaskStatus, UserAnswers}
import pages.*
import pages.Page.NOTIFICATION_PATH
import pages.combined.CombinedNotificationConfirmationPage
import pages.notification.*
import play.api.libs.json.*

enum CombinedStage(
    val provideSaoDetailsStatus: TaskStatus = NotStarted,
    val uploadTemplateStatus: TaskStatus = CannotStartYet,
    val submitNotificationStatus: TaskStatus = CannotStartYet,
    val submitCertificateStatus: TaskStatus = CannotStartYet
) {
  case ProvideSaoDetails extends CombinedStage()
  case UploadSubmissionTemplateDetails
      extends CombinedStage(
        provideSaoDetailsStatus = Completed,
        uploadTemplateStatus = NotStarted,
        submitNotificationStatus = CannotStartYet,
        submitCertificateStatus = CannotStartYet
      )
  case SubmitNotificationInfo
      extends CombinedStage(
        provideSaoDetailsStatus = Completed,
        uploadTemplateStatus = Completed,
        submitNotificationStatus = NotStarted,
        submitCertificateStatus = CannotStartYet
      )

  case SubmitCertificateInfo
    extends CombinedStage(
      provideSaoDetailsStatus = Completed,
      uploadTemplateStatus = Completed,
      submitNotificationStatus = Completed,
      submitCertificateStatus = NotStarted
  )

  case AllStagesCompleted
      extends CombinedStage(
        provideSaoDetailsStatus = Completed,
        uploadTemplateStatus = Completed,
        submitNotificationStatus = Completed,
        submitCertificateStatus = Completed
      )
}

object CombinedStage {

  def taskListStage(userAnswers: UserAnswers): CombinedStage =
    if !isProvideSaoDetailsComplete(userAnswers) then {
      ProvideSaoDetails
    } else if !isUploadNotificationTemplateComplete(userAnswers) then {
      UploadSubmissionTemplateDetails
    } else if !isSubmitNotificationComplete(userAnswers) then {
      SubmitNotificationInfo
    } else {
      SubmitCertificateInfo
    }

  def canStartUploadNotificationTemplate(userAnswers: UserAnswers): Boolean =
    isProvideSaoDetailsComplete(userAnswers)

  def canStartSubmitNotification(userAnswers: UserAnswers): Boolean =
    isProvideSaoDetailsComplete(userAnswers) && isUploadNotificationTemplateComplete(userAnswers)

  def canStartSubmitCertificate(userAnswers: UserAnswers): Boolean = {
    isProvideSaoDetailsComplete(userAnswers) && isUploadNotificationTemplateComplete(userAnswers) && isSubmitNotificationComplete(userAnswers)
  }

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

  private def isSubmitNotificationComplete(userAnswers: UserAnswers): Boolean =
    userAnswers.get(CombinedNotificationConfirmationPage).contains(true)
}
