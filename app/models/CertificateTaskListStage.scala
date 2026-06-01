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

import play.api.mvc.JavascriptLiteral

enum CertificateTaskListStage {
  case ProvideSaoDetailsStageActive, UploadSubmissionTemplateStageActive, SubmitCertificateStageActive, Complete
}

object CertificateTaskListStage {
  given jsLiteral: JavascriptLiteral[CertificateTaskListStage] = new JavascriptLiteral[CertificateTaskListStage] {
    override def to(value: CertificateTaskListStage): String = value match {
      case ProvideSaoDetailsStageActive        => "ProvideSaoDetailsStageActive"
      case UploadSubmissionTemplateStageActive => "UploadSubmissionTemplateStageActive"
      case SubmitCertificateStageActive        => "SubmitCertificateStageActive"
      case Complete                            => "Complete"
    }
  }

  extension (stage: CertificateTaskListStage) {
    def toState(): CertificateTaskListState = {
      stage match {
        case CertificateTaskListStage.ProvideSaoDetailsStageActive =>
          CertificateTaskListState(
            CertificateTaskListStatus.NotStarted,
            CertificateTaskListStatus.CannotStartYet,
            CertificateTaskListStatus.CannotStartYet,
            CertificateTaskListShowContinueButton.NotShown
          )
        case CertificateTaskListStage.UploadSubmissionTemplateStageActive =>
          CertificateTaskListState(
            CertificateTaskListStatus.Completed,
            CertificateTaskListStatus.NotStarted,
            CertificateTaskListStatus.CannotStartYet,
            CertificateTaskListShowContinueButton.NotShown
          )
        case CertificateTaskListStage.SubmitCertificateStageActive =>
          CertificateTaskListState(
            CertificateTaskListStatus.Completed,
            CertificateTaskListStatus.Completed,
            CertificateTaskListStatus.NotStarted,
            CertificateTaskListShowContinueButton.NotShown
          )
        case CertificateTaskListStage.Complete =>
          CertificateTaskListState(
            CertificateTaskListStatus.Completed,
            CertificateTaskListStatus.Completed,
            CertificateTaskListStatus.Completed,
            CertificateTaskListShowContinueButton.Shown
          )
      }
    }
  }
}
