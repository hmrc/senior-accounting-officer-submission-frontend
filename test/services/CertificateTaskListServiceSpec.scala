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

import base.SpecBase
import models.NormalMode
import models.certificate.CertificateDeclarationStandIn
import models.certificate.CertificateStage.*
import models.certificate.CertificateWhoIsSubmitting
import models.upload.UploadTemplateTableData
import pages.certificate.*

class CertificateTaskListServiceSpec extends SpecBase {
  def SUT = new CertificateTaskListService

  "determineStage" - {
    "empty user answers results in sao details stage" in {
      SUT.determineStage(emptyUserAnswers) mustBe ProvideSaoDetailsStage
    }

    "user answers with sao details results in submission template stage" in {
      val userAnswers = emptyUserAnswers
        .add(CertificateSaoFullNamePage, "Firstname Lastname")
        .add(CertificateSaoEmailPage, "example@example.com")

      SUT.determineStage(userAnswers) mustBe UploadSubmissionTemplateStage
    }

    "user answers with partial sao details results in sao details stage" in {
      val userAnswers = emptyUserAnswers
        .add(CertificateSaoFullNamePage, "Firstname Lastname")

      SUT.determineStage(userAnswers) mustBe ProvideSaoDetailsStage
    }

    "user answers with submitted template results in submit certificate stage" in {
      val userAnswers = emptyUserAnswers
        .add(CertificateSaoFullNamePage, "Firstname Lastname")
        .add(CertificateSaoEmailPage, "example@example.com")
        .add(CertificateReviewQualifiedPage, true)
        .add(CertificateReviewUnqualifiedPage, true)
        .add(
          CertificateUploadTemplateTablePage,
          UploadTemplateTableData(
            rows = Nil,
            errors = Nil
          )
        )

      SUT.determineStage(userAnswers) mustBe SubmitCertificateStage
    }

    "user answers with partially submitted template results in submission template stage" in {
      val userAnswers = emptyUserAnswers
        .add(CertificateSaoFullNamePage, "Firstname Lastname")
        .add(CertificateSaoEmailPage, "example@example.com")
        .add(CertificateReviewQualifiedPage, true)

      SUT.determineStage(userAnswers) mustBe UploadSubmissionTemplateStage
    }
  }
}
