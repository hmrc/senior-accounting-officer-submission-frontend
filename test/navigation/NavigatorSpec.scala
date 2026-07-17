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
import controllers.notification.routes as notificationRoutes
import controllers.routes
import models.*
import models.certificate.{CertificateTaskListStage, CertificateWhoIsSubmitting}
import models.upload.UploadTemplateTableData
import pages.*
import pages.certificate.*
import pages.notification.*

import java.time.LocalDate

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator()

  "Navigator.nextPage" - {

    "in Normal mode" - {

      "must throw an not-implemented error for an unspecified configuration" in {
        case object UnknownPage extends Page
        intercept[NotImplementedError] {
          navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers)
        }
      }

      "when on NotificationAdditionalInformationPage, must go to confirm your notification page" in {
        navigator.nextPage(
          NotificationAdditionalInformationPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.ConfirmYourNotificationController.onPageLoad()
      }

      "when on ConfirmYourNotificationPage, must go to check your answers page" in {
        navigator.nextPage(
          ConfirmYourNotificationPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on ConfirmYourNotificationPage, must go to notification check your answers page" in {
        navigator.nextPage(
          ConfirmYourNotificationPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on IsThisTheSaoOnCertificatePage and the user selected Yes, must go to SAO email page" in {
        navigator.nextPage(
          IsThisTheSaoOnCertificatePage,
          NormalMode,
          emptyUserAnswers.set(IsThisTheSaoOnCertificatePage, true).get
        ) mustBe routes.SaoEmailController.onPageLoad(NormalMode)
      }

      "when on IsThisTheSaoOnCertificatePage and the user selected No, must go to SAO name page" in {
        navigator.nextPage(
          IsThisTheSaoOnCertificatePage,
          NormalMode,
          emptyUserAnswers.set(IsThisTheSaoOnCertificatePage, false).get
        ) mustBe routes.SaoNameController.onPageLoad(NormalMode)
      }

      "when on IsThisTheSaoOnCertificatePage and the question is not answered then" in {
        intercept[NotImplementedError] {
          navigator.nextPage(
            IsThisTheSaoOnCertificatePage,
            NormalMode,
            emptyUserAnswers
          )
        }
      }

      "when on SaoNamePage, must go to SAO email page" in {
        navigator.nextPage(
          SaoNamePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.SaoEmailController.onPageLoad(NormalMode)
      }

      "when on SaoEmailPage, must go to SAO email communication choice page" in {
        navigator.nextPage(
          SaoEmailPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.SaoEmailCommunicationChoiceController.onPageLoad(NormalMode)
      }

      "when on SaoEmailCommunicationChoicePage, must go to certificate check your answers page" in {
        navigator.nextPage(
          SaoEmailCommunicationChoicePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.CombinedCertificateCheckYourAnswersController.onPageLoad()
      }

      "when on CombinedCertificateCheckYourAnswersPage, must go to who submits certificate page" in {
        navigator.nextPage(
          CombinedCertificateCheckYourAnswersPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.CombinedWhoSubmitsCertificateController.onPageLoad(NormalMode)
      }

      "when on CombinedWhoSubmitsCertificatePage, must go to qualified companies page" in {
        navigator.nextPage(
          CombinedWhoSubmitsCertificatePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe certificateRoutes.QualifiedCompaniesController.onPageLoad()
      }

      "when on QualifiedCompaniesPage, must go to unqualified companies page" in {
        navigator.nextPage(
          QualifiedCompaniesPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe certificateRoutes.UnqualifiedCompaniesController.onPageLoad()
      }

      "when on UnqualifiedCompaniesPage, must go to certificate submission declaration page" in {
        navigator.nextPage(
          UnqualifiedCompaniesPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.CombinedCertificateDeclarationSaoController.onPageLoad(NormalMode)
      }

      "when on CombinedCertificateDeclarationSaoPage, must go to certificate confirmation page" in {
        navigator.nextPage(
          CombinedCertificateDeclarationSaoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.CombinedCertificateConfirmationController.onPageLoad()
      }

      "when on NotificationConfirmationPage, must go to notification task list" in {
        navigator.nextPage(
          NotificationConfirmationPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationTaskListController.onComplete()
      }

      "when on NotificationMoreThanOneSaoPage and the user selected No, must go to Sao name page" in {
        navigator.nextPage(
          NotificationMoreThanOneSaoPage,
          NormalMode,
          emptyUserAnswers.set(NotificationMoreThanOneSaoPage, false).success.value
        ) mustBe notificationRoutes.NotificationSingleSaoOfficerNameController.onPageLoad(NormalMode)
      }

      "when on NotificationMoreThanOneSaoPage and the user selected Yes, must go to multiple sao name page" in {
        navigator.nextPage(
          NotificationMoreThanOneSaoPage,
          NormalMode,
          emptyUserAnswers.set(NotificationMoreThanOneSaoPage, true).success.value
        ) mustBe notificationRoutes.NotificationMultiSaoLastOfficerNameController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoLastOfficerNameController, must go to more sao submit notification first date page" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerNamePage,
          NormalMode,
          emptyUserAnswers.set(NotificationMoreThanOneSaoPage, true).success.value
        ) mustBe notificationRoutes.NotificationMultiSaoLastOfficerStartDateController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoLastOfficerStartDatePage, must go to who was the sao before page" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerStartDatePage,
          NormalMode,
          emptyUserAnswers.set(NotificationMultiSaoLastOfficerStartDatePage, LocalDate.of(2026, 5, 1)).success.value
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoPreviousOfficerNamePage, must go to NotificationMultiSaoPreviousOfficerStartDate" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerNamePage(0),
          NormalMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerStartDateController.onPageLoad(NormalMode, 0)
      }

      "when on NotificationMultiSaoPreviousOfficerStartDatePage, must go to NotificationMultiSaoPreviousOfficerEndDate page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerStartDatePage(0),
          NormalMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerEndDateController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoPreviousOfficerEndDatePage, must go to NotificationMultiSaoAreAllAdded page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerEndDatePage(0),
          NormalMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationMultiSaoAreAllAddedController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoAreAllAddedPage, and no response is in the database, must throw an exception" in {
        intercept[NotImplementedError] {
          navigator.nextPage(
            NotificationMultiSaoAreAllAddedPage(0),
            NormalMode,
            emptyUserAnswers
          )
        }
      }

      "when on NotificationMultiSaoAreAllAddedPage, and the user answers yes, must go to the notification task list" in {
        navigator.nextPage(
          NotificationMultiSaoAreAllAddedPage(0),
          NormalMode,
          emptyUserAnswers.set(NotificationMultiSaoAreAllAddedPage(0), true).success.value
        ) mustBe notificationRoutes.NotificationTaskListController.onPageLoad()
      }

      "when on NotificationMultiSaoAreAllAddedPage, and the user answers no, must go to NotificationMultiSaoPreviousOfficerName page with an incremented saoIndex" in {
        navigator.nextPage(
          NotificationMultiSaoAreAllAddedPage(0),
          NormalMode,
          emptyUserAnswers.set(NotificationMultiSaoAreAllAddedPage(0), false).success.value
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode, 1)
      }

      "when on NotificationSingleSaoOfficerNamePage, must go to the submit notification start page" in {
        navigator.nextPage(
          NotificationSingleSaoOfficerNamePage,
          NormalMode,
          emptyUserAnswers.set(NotificationSingleSaoOfficerNamePage, "Firstname Lastname").success.value
        ) mustBe notificationRoutes.NotificationTaskListController.onPageLoad()
      }

      "when on UploadTemplateTablePage with no parsing errors, must go to notification start page" in {
        val userAnswers =
          emptyUserAnswers
            .set(UploadTemplateTablePage, UploadTemplateTableData(rows = Seq.empty, errors = Seq.empty))
            .success
            .value

        navigator.nextPage(
          UploadTemplateTablePage,
          NormalMode,
          userAnswers
        ) mustBe notificationRoutes.NotificationTaskListController.onPageLoad()
      }

      "when on UploadTemplateTablePage with parsing errors, must go to upload form page" in {
        val userAnswers =
          emptyUserAnswers
            .set(
              UploadTemplateTablePage,
              UploadTemplateTableData(
                rows = Seq.empty,
                errors = Seq(models.upload.TemplateParseError(9, Some("Company UTR"), "missing_required_value", "x"))
              )
            )
            .success
            .value

        navigator.nextPage(
          UploadTemplateTablePage,
          NormalMode,
          userAnswers
        ) mustBe notificationRoutes.NotificationUploadFormController.onPageLoad()
      }

      "when on UploadTemplateTableErrorPage  must go to upload form page" in {
        val userAnswers =
          emptyUserAnswers
            .set(
              UploadTemplateTablePage,
              UploadTemplateTableData(
                rows = Seq.empty,
                errors = Seq(models.upload.TemplateParseError(9, Some("Company UTR"), "missing_required_value", "x"))
              )
            )
            .success
            .value

        navigator.nextPage(
          UploadTemplateTableErrorPage,
          NormalMode,
          userAnswers
        ) mustBe notificationRoutes.NotificationUploadFormController.onPageLoad()
      }

      "when on UploadTemplateTablePage with no upload data, must go to journey recovery page" in {
        navigator.nextPage(
          UploadTemplateTablePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "SubmissionTypePage" - {

        "when on SubmissionTypePage and the user chose notification only, must go to NotificationTaskList" in {
          navigator.nextPage(
            SubmissionTypePage,
            NormalMode,
            emptyUserAnswers.set(SubmissionTypePage, SubmissionType.Notification).get
          ) mustBe notificationRoutes.NotificationTaskListController.onPageLoad()
        }

        "when on SubmissionTypePage and the user chose certificate only, must go to CertificateTaskList" in {
          navigator.nextPage(
            SubmissionTypePage,
            NormalMode,
            emptyUserAnswers.set(SubmissionTypePage, SubmissionType.Certificate).get
          ) mustBe certificateRoutes.CertificateTaskListController.onPageLoad(
            CertificateTaskListStage.ProvideSaoDetailsStage
          )
        }

        "when on SubmissionTypePage and the user chose both the notification and the certificate, must throw an exception" in {
          intercept[NotImplementedError] {
            navigator.nextPage(
              SubmissionTypePage,
              NormalMode,
              emptyUserAnswers.set(SubmissionTypePage, SubmissionType.Combined).get
            )
          }
        }
      }

      // certificate flow

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

      "when on CertificateUploadTemplateTableErrorPage with no parsing errors, must go to upload form page" in {
        val userAnswers =
          emptyUserAnswers
            .set(CertificateUploadTemplateTablePage, UploadTemplateTableData(rows = Seq.empty, errors = Seq.empty))
            .success
            .value

        navigator.nextPage(
          CertificateUploadTemplateTableErrorPage,
          NormalMode,
          userAnswers
        ) mustBe certificateRoutes.CertificateUploadFormController.onPageLoad()
      }

      "when on CertificateUploadTemplateTableErrorPage with parsing errors, must go to upload form page" in {
        val userAnswers =
          emptyUserAnswers
            .set(
              CertificateUploadTemplateTablePage,
              UploadTemplateTableData(
                rows = Seq.empty,
                errors = Seq(models.upload.TemplateParseError(9, Some("Company UTR"), "missing_required_value", "x"))
              )
            )
            .success
            .value

        navigator.nextPage(
          CertificateUploadTemplateTableErrorPage,
          NormalMode,
          userAnswers
        ) mustBe certificateRoutes.CertificateUploadFormController.onPageLoad()
      }

      "when on CertificateUploadTemplateTableErrorPage with no upload data, must go to journey recovery page" in {
        navigator.nextPage(
          CertificateUploadTemplateTableErrorPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
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

      "when on NotificationAdditionalInformationPage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationAdditionalInformationPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on SaoNamePage, must go to certificate check your answers page" in {
        navigator.nextPage(
          SaoNamePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CombinedCertificateCheckYourAnswersController.onPageLoad()
      }

      "when on CertificateAdditionalInformationPage, must go to certificate check your answers page" in {
        navigator.nextPage(
          CertificateAdditionalInformationPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
      }

      "when on SaoEmailPage, must go to certificate check your answers page" in {
        navigator.nextPage(
          SaoEmailPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CombinedCertificateCheckYourAnswersController.onPageLoad()
      }

      "when on SaoEmailCommunicationChoicePage, must go to certificate check your answers page" in {
        navigator.nextPage(
          SaoEmailCommunicationChoicePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CombinedCertificateCheckYourAnswersController.onPageLoad()
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
