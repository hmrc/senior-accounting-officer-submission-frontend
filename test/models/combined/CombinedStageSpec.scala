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

import base.SpecBase
import models.combined.CombinedStage.*
import models.upload.UploadTemplateTableData
import models.{NormalMode, TaskStatus}
import org.scalatest.OptionValues
import pages.combined.CombinedNotificationConfirmationPage
import pages.notification.*

class CombinedStageSpec extends SpecBase with OptionValues {

  "taskListStage" - {

    "return ProvideSaoDetails when SAO details are incomplete" in {

      val answers = emptyUserAnswers

      CombinedStage.taskListStage(answers) mustBe ProvideSaoDetails
    }

    "return UploadSubmissionTemplateDetails when SAO details are complete but template upload is incomplete" in {

      val answers =
        emptyUserAnswers
          .add(NotificationMoreThanOneSaoPage(NormalMode), false)
          .add(NotificationSingleSaoOfficerNamePage(NormalMode), "Test SAO")

      CombinedStage.taskListStage(answers) mustBe UploadSubmissionTemplateDetails
    }

    "return SubmitNotificationInfo when template upload is complete but notification not submitted" in {

      val answers =
        emptyUserAnswers
          .add(NotificationMoreThanOneSaoPage(NormalMode), false)
          .add(NotificationSingleSaoOfficerNamePage(NormalMode), "Test SAO")
          .add(UploadTemplateTablePage, UploadTemplateTableData(rows = Seq.empty, errors = Nil))
          .add(UploadTemplateReviewPage, true)

      CombinedStage.taskListStage(answers) mustBe SubmitNotificationInfo
    }

    "return SubmitCertificateInfo when notification has been submitted" in {

      val answers =
        emptyUserAnswers
          .add(NotificationMoreThanOneSaoPage(NormalMode), false)
          .add(NotificationSingleSaoOfficerNamePage(NormalMode), "Test SAO")
          .add(UploadTemplateTablePage, UploadTemplateTableData(rows = Seq.empty, errors = Nil))
          .add(UploadTemplateReviewPage, true)
          .add(CombinedNotificationConfirmationPage, true)

      CombinedStage.taskListStage(answers) mustBe SubmitCertificateInfo
    }
  }

  "canStartUploadNotificationTemplate" - {

    "return false when SAO details are incomplete" in {

      CombinedStage.canStartUploadNotificationTemplate(emptyUserAnswers) mustBe false
    }

    "return true when SAO details are complete" in {

      val answers =
        emptyUserAnswers
          .add(NotificationMoreThanOneSaoPage(NormalMode), false)
          .add(NotificationSingleSaoOfficerNamePage(NormalMode), "Test SAO")

      CombinedStage.canStartUploadNotificationTemplate(answers) mustBe true
    }
  }

  "canStartSubmitNotification" - {

    "return false when upload template step is incomplete" in {

      val answers =
        emptyUserAnswers
          .add(NotificationMoreThanOneSaoPage(NormalMode), false)
          .add(NotificationSingleSaoOfficerNamePage(NormalMode), "Test SAO")

      CombinedStage.canStartSubmitNotification(answers) mustBe false
    }

    "return true when SAO details and template upload are complete" in {

      val answers =
        emptyUserAnswers
          .add(NotificationMoreThanOneSaoPage(NormalMode), false)
          .add(NotificationSingleSaoOfficerNamePage(NormalMode), "Test SAO")
          .add(UploadTemplateTablePage, UploadTemplateTableData(rows = Seq.empty, errors = Nil))
          .add(UploadTemplateReviewPage, true)

      CombinedStage.canStartSubmitNotification(answers) mustBe true
    }
  }

  "canStartSubmitCertificate" - {

    "return false when notification has not been submitted" in {

      val answers =
        emptyUserAnswers
          .set(NotificationMoreThanOneSaoPage(NormalMode), false)
          .success
          .value
          .set(NotificationSingleSaoOfficerNamePage(NormalMode), "Test SAO")
          .success
          .value
          .set(UploadTemplateTablePage, UploadTemplateTableData(rows = Seq.empty, errors = Nil))
          .success
          .value
          .set(UploadTemplateReviewPage, true)
          .success
          .value

      CombinedStage.canStartSubmitCertificate(answers) mustBe false
    }

    "return true when all previous stages are complete" in {

      val answers =
        emptyUserAnswers
          .add(NotificationMoreThanOneSaoPage(NormalMode), false)
          .add(NotificationSingleSaoOfficerNamePage(NormalMode), "Test SAO")
          .add(UploadTemplateTablePage, UploadTemplateTableData(rows = Seq.empty, errors = Nil))
          .add(UploadTemplateReviewPage, true)
          .add(CombinedNotificationConfirmationPage, true)

      CombinedStage.canStartSubmitCertificate(answers) mustBe true
    }
  }

  "enum statuses" - {

    "have expected statuses for ProvideSaoDetails" in {

      ProvideSaoDetails.provideSaoDetailsStatus mustBe TaskStatus.NotStarted
      ProvideSaoDetails.uploadTemplateStatus mustBe TaskStatus.CannotStartYet
      ProvideSaoDetails.submitNotificationStatus mustBe TaskStatus.CannotStartYet
      ProvideSaoDetails.submitCertificateStatus mustBe TaskStatus.CannotStartYet
    }

    "have expected statuses for UploadSubmissionTemplateDetails" in {
      UploadSubmissionTemplateDetails.provideSaoDetailsStatus mustBe TaskStatus.Completed
      UploadSubmissionTemplateDetails.uploadTemplateStatus mustBe TaskStatus.NotStarted
      UploadSubmissionTemplateDetails.submitNotificationStatus mustBe TaskStatus.CannotStartYet
      UploadSubmissionTemplateDetails.submitCertificateStatus mustBe TaskStatus.CannotStartYet
    }

    "have expected statuses for SubmitNotificationInfo" in {
      SubmitNotificationInfo.provideSaoDetailsStatus mustBe TaskStatus.Completed
      SubmitNotificationInfo.uploadTemplateStatus mustBe TaskStatus.Completed
      SubmitNotificationInfo.submitNotificationStatus mustBe TaskStatus.NotStarted
      SubmitNotificationInfo.submitCertificateStatus mustBe TaskStatus.CannotStartYet
    }

    "have expected statuses for SubmitCertificateInfo" in {
      SubmitCertificateInfo.provideSaoDetailsStatus mustBe TaskStatus.Completed
      SubmitCertificateInfo.uploadTemplateStatus mustBe TaskStatus.Completed
      SubmitCertificateInfo.submitNotificationStatus mustBe TaskStatus.Completed
      SubmitCertificateInfo.submitCertificateStatus mustBe TaskStatus.NotStarted
    }

    "have expected statuses for AllStagesCompleted" in {

      AllStagesCompleted.provideSaoDetailsStatus mustBe TaskStatus.Completed
      AllStagesCompleted.uploadTemplateStatus mustBe TaskStatus.Completed
      AllStagesCompleted.submitNotificationStatus mustBe TaskStatus.Completed
      AllStagesCompleted.submitCertificateStatus mustBe TaskStatus.Completed
    }
  }
}
