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

enum SubmitNotificationStatus {
  case CannotStartYet, NotStarted, Completed
}

enum SubmitNotificationStage(
    val uploadNotificationTemplateStatus: SubmitNotificationStatus = NotStarted,
    val submitNotificationStatus: SubmitNotificationStatus = CannotStartYet
) {
  case UploadSubmissionTemplateDetails extends SubmitNotificationStage()
  case SubmitNotificationInfo
      extends SubmitNotificationStage(
        uploadNotificationTemplateStatus = Completed,
        submitNotificationStatus = NotStarted
      )

  // Todo : To be removed when screen is lockdown
  case ShowAllLinks
      extends SubmitNotificationStage(
        uploadNotificationTemplateStatus = NotStarted,
        submitNotificationStatus = NotStarted
      )
}
