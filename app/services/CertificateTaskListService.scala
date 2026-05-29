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

package services

import models.{CertificateTaskListStage, CertificateTaskListState, CertificateTaskListStatus}

class CertificateTaskListService {
  // TODO: don't call it f
  def f(stage: CertificateTaskListStage): CertificateTaskListState = {
    stage match {
      case CertificateTaskListStage.Stage1Active =>
        CertificateTaskListState(
          CertificateTaskListStatus.NotStarted,
          CertificateTaskListStatus.CannotStartYet,
          CertificateTaskListStatus.CannotStartYet,
          false
        )
      case CertificateTaskListStage.Stage2Active =>
        CertificateTaskListState(
          CertificateTaskListStatus.Completed,
          CertificateTaskListStatus.NotStarted,
          CertificateTaskListStatus.CannotStartYet,
          false
        )
      case CertificateTaskListStage.Stage3Active =>
        CertificateTaskListState(
          CertificateTaskListStatus.Completed,
          CertificateTaskListStatus.Completed,
          CertificateTaskListStatus.NotStarted,
          false
        )
      case CertificateTaskListStage.Complete =>
        CertificateTaskListState(
          CertificateTaskListStatus.Completed,
          CertificateTaskListStatus.Completed,
          CertificateTaskListStatus.Completed,
          true
        )
    }
  }
}
