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

import models.*
import models.UserAnswers
import models.certificate.CertificateStage
import models.certificate.CertificateStage.*
import models.certificate.CertificateWhoIsSubmitting.*
import pages.certificate.*

class CertificateTaskListService {
  def determineStage(userAnswers: UserAnswers): CertificateStage = {
    (userAnswers.hasSaoDetails, userAnswers.hasSubmissionTemplate, userAnswers.isCertificateSubmitted) match {
      case (true, true, true) => Complete
      case (true, true, _)    => SubmitCertificateStage
      case (true, _, _)       => UploadSubmissionTemplateStage
      case (_, _, _)          => ProvideSaoDetailsStage
    }
  }

  extension (userAnswers: UserAnswers) {
    private def hasSaoDetails: Boolean = {
      (for {
        _ <- userAnswers.get(CertificateSaoFullNamePage)
        _ <- userAnswers.get(CertificateSaoEmailPage)
      } yield true).getOrElse(false)
    }

    private def hasSubmissionTemplate: Boolean = {
      (for {
        _ <- userAnswers.get(CertificateReviewQualifiedPage)
        _ <- userAnswers.get(CertificateReviewUnqualifiedPage)
        _ <- userAnswers.get(CertificateUploadTemplateTablePage)
      } yield true).getOrElse(false)
    }

    private def isCertificateSubmitted: Boolean = {
      (for {
        _               <- userAnswers.getNullable(CertificateAdditionalInformationPage)
        declarationKind <- userAnswers.get(CertificateWhoIsSubmittingPage(NormalMode))
        saoCompleted     = userAnswers.get(CertificateDeclarationSaoPage(NormalMode)).nonEmpty && declarationKind == Sao
        standInCompleted = userAnswers
          .get(CertificateDeclarationStandInPage(NormalMode))
          .nonEmpty && declarationKind == StandIn
        if saoCompleted || standInCompleted
      } yield true).getOrElse(false)
    }
  }
}
