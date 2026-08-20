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

package navigation

import base.SpecBase
import controllers.certificate.routes as certificateRoutes
import models.*
import models.certificate.{CertificateTaskListStage, CertificateWhoIsSubmitting}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.*
import pages.certificate.*

class CertificateNavigatorSpec extends SpecBase with GuiceOneAppPerSuite {

  lazy val navigator: Navigator = app.injector.instanceOf[CertificateNavigator]

  "CertificateNavigator.nextPage" - {

    "in Normal mode" - {

      "must throw an not-implemented error for an unspecified configuration" in {
        case object UnknownPage extends Page
        intercept[NotImplementedError] {
          navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers)
        }
      }

      "when on CertificateSaoFullName, must go to CertificateSaoEmail page" in {
        navigator.nextPage(
          CertificateSaoFullNamePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe certificateRoutes.CertificateSaoEmailController.onPageLoad(NormalMode)
      }

      "when on CertificateSaoEmail, must go to CertificateTaskList page" in {
        navigator.nextPage(
          CertificateSaoEmailPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe certificateRoutes.CertificateTaskListController.onPageLoad(
          CertificateTaskListStage.UploadSubmissionTemplateStage
        )
      }

      "when on CertificateReviewQualified, must go to CertificateReviewUnqualified page" in {
        navigator.nextPage(
          CertificateReviewQualifiedPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe certificateRoutes.CertificateReviewUnqualifiedController.onPageLoad()
      }

      "when on CertificateReviewUnqualified, must go to CertificateTaskList page" in {
        navigator.nextPage(
          CertificateReviewUnqualifiedPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe certificateRoutes.CertificateTaskListController.onPageLoad(
          CertificateTaskListStage.SubmitCertificateStage
        )
      }

      "when on CertificateAdditionalInformation, must go to CertificateWhoIsSubmitting page" in {
        navigator.nextPage(
          CertificateAdditionalInformationPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe certificateRoutes.CertificateWhoIsSubmittingController.onPageLoad(NormalMode)
      }

      "when on CertificateWhoIsSubmitting, must go to CertificateDeclarationSao page" in {
        navigator.nextPage(
          CertificateWhoIsSubmittingPage,
          NormalMode,
          emptyUserAnswers.set(CertificateWhoIsSubmittingPage, CertificateWhoIsSubmitting.Sao).get
        ) mustBe certificateRoutes.CertificateDeclarationSaoController.onPageLoad(NormalMode)
      }

      "when on CertificateWhoIsSubmitting, must go to CertificateDeclarationStandIn page" in {
        navigator.nextPage(
          CertificateWhoIsSubmittingPage,
          NormalMode,
          emptyUserAnswers.set(CertificateWhoIsSubmittingPage, CertificateWhoIsSubmitting.StandIn).get
        ) mustBe certificateRoutes.CertificateDeclarationStandInController.onPageLoad(NormalMode)
      }

      "when on CertificateDeclarationSao, must go to CertificateCheckYourAnswers page" in {
        navigator.nextPage(
          CertificateDeclarationSaoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
      }

      "when on CertificateDeclarationStandIn, must go to CertificateCheckYourAnswers page" in {
        navigator.nextPage(
          CertificateDeclarationStandInPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
      }

      "when on CertificateConfirmation, must go to CertificateTaskList page" in {
        navigator.nextPage(
          CertificateConfirmationPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe certificateRoutes.CertificateTaskListController.onPageLoad(CertificateTaskListStage.Complete)
      }
    }

    "in Check mode" - {

      "when on CertificateAdditionalInformationPage, must go to certificate check your answers page" in {
        navigator.nextPage(
          CertificateAdditionalInformationPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
      }

      "must throw an not-implemented error for an unspecified configuration" in {
        case object UnknownPage extends Page
        intercept[NotImplementedError] {
          navigator.nextPage(UnknownPage, CheckMode, emptyUserAnswers)
        }
      }

    }
  }
}
