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

package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class CertificateTaskListStageSpec extends AnyFreeSpec with Matchers {

  "CertificateTaskListStage.toState" - {
    "must return correct state for provide sao details stage" in {
      val stage  = CertificateTaskListStage.ProvideSaoDetailsStage
      val result = stage.toState()

      val expected = CertificateTaskListState(
        provideSaoDetailsStage = TaskStatus.NotStarted,
        uploadSubmissionTemplateStage = TaskStatus.CannotStartYet,
        submitCertificateStage = TaskStatus.CannotStartYet,
        showContinueButton = false
      )

      result mustBe expected
    }

    "must return correct state for upload submission template stage" in {
      val stage  = CertificateTaskListStage.UploadSubmissionTemplateStage
      val result = stage.toState()

      val expected = CertificateTaskListState(
        provideSaoDetailsStage = TaskStatus.Completed,
        uploadSubmissionTemplateStage = TaskStatus.NotStarted,
        submitCertificateStage = TaskStatus.CannotStartYet,
        showContinueButton = false
      )

      result mustBe expected
    }

    "must return correct state for submit certificate stage" in {

      val stage  = CertificateTaskListStage.SubmitCertificateStage
      val result = stage.toState()

      val expected = CertificateTaskListState(
        provideSaoDetailsStage = TaskStatus.Completed,
        uploadSubmissionTemplateStage = TaskStatus.Completed,
        submitCertificateStage = TaskStatus.NotStarted,
        showContinueButton = false
      )

      result mustBe expected
    }

    "must return correct state for completed stage" in {

      val stage  = CertificateTaskListStage.Complete
      val result = stage.toState()

      val expected = CertificateTaskListState(
        provideSaoDetailsStage = TaskStatus.Completed,
        uploadSubmissionTemplateStage = TaskStatus.Completed,
        submitCertificateStage = TaskStatus.Completed,
        showContinueButton = true
      )

      result mustBe expected
    }
  }
}
