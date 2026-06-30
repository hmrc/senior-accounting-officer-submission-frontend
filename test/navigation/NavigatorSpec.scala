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
          navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id"))
        }
      }

      "when on NotificationAdditionalInformationPage, must go to confirm your notification page" in {
        navigator.nextPage(
          NotificationAdditionalInformationPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe notificationRoutes.ConfirmYourNotificationController.onPageLoad()
      }

      "when on ConfirmYourNotificationPage, must go to check your answers page" in {
        navigator.nextPage(
          ConfirmYourNotificationPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationCheckYourAnswersPage, must go to notification confirmation page" in {
        val notRefIdMock = "SAONOT0123456789"
        navigator.nextPage(
          NotificationCheckYourAnswersPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe notificationRoutes.NotificationConfirmationController.onPageLoad(notRefIdMock)
      }

      "when on ConfirmYourNotificationPage, must go to notification check your answers page" in {
        navigator.nextPage(
          ConfirmYourNotificationPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on IsThisTheSaoOnCertificatePage and the user selected Yes, must go to SAO email page" in {
        navigator.nextPage(
          IsThisTheSaoOnCertificatePage,
          NormalMode,
          UserAnswers("id").set(IsThisTheSaoOnCertificatePage, true).get
        ) mustBe routes.SaoEmailController.onPageLoad(NormalMode)
      }

      "when on IsThisTheSaoOnCertificatePage and the user selected No, must go to SAO name page" in {
        navigator.nextPage(
          IsThisTheSaoOnCertificatePage,
          NormalMode,
          UserAnswers("id").set(IsThisTheSaoOnCertificatePage, false).get
        ) mustBe routes.SaoNameController.onPageLoad(NormalMode)
      }

      "when on IsThisTheSaoOnCertificatePage and the question is not answered then" in {
        intercept[NotImplementedError] {
          navigator.nextPage(
            IsThisTheSaoOnCertificatePage,
            NormalMode,
            UserAnswers("id")
          )
        }
      }

      "when on SaoNamePage, must go to SAO email page" in {
        navigator.nextPage(
          SaoNamePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.SaoEmailController.onPageLoad(NormalMode)
      }

      "when on SaoEmailPage, must go to SAO email communication choice page" in {
        navigator.nextPage(
          SaoEmailPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.SaoEmailCommunicationChoiceController.onPageLoad(NormalMode)
      }

      "when on SaoEmailCommunicationChoicePage, must go to certificate check your answers page" in {
        navigator.nextPage(
          SaoEmailCommunicationChoicePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.CombinedCertificateCheckYourAnswersController.onPageLoad()
      }

      "when on CombinedCertificateCheckYourAnswersPage, must go to who submits certificate page" in {
        navigator.nextPage(
          CombinedCertificateCheckYourAnswersPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.CombinedWhoSubmitsCertificateController.onPageLoad(NormalMode)
      }

      "when on CombinedWhoSubmitsCertificatePage, must go to qualified companies page" in {
        navigator.nextPage(
          CombinedWhoSubmitsCertificatePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe certificateRoutes.QualifiedCompaniesController.onPageLoad()
      }

      "when on QualifiedCompaniesPage, must go to unqualified companies page" in {
        navigator.nextPage(
          QualifiedCompaniesPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe certificateRoutes.UnqualifiedCompaniesController.onPageLoad()
      }

      "when on UnqualifiedCompaniesPage, must go to certificate submission declaration page" in {
        navigator.nextPage(
          UnqualifiedCompaniesPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.CombinedCertificateDeclarationSaoController.onPageLoad(NormalMode)
      }

      "when on CombinedCertificateDeclarationSaoPage, must go to certificate confirmation page" in {
        navigator.nextPage(
          CombinedCertificateDeclarationSaoPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.CombinedCertificateConfirmationController.onPageLoad()
      }

      "when on NotificationConfirmationPage, must go to notification task list" in {
        navigator.nextPage(
          NotificationConfirmationPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe notificationRoutes.NotificationTaskListController.onComplete()
      }

      "when on NotificationMoreThanOneSaoPage and the user selected No, must go to Sao name page" in {
        navigator.nextPage(
          NotificationMoreThanOneSaoPage,
          NormalMode,
          UserAnswers("id").set(NotificationMoreThanOneSaoPage, false).success.value
        ) mustBe notificationRoutes.NotificationSingleSaoOfficerNameController.onPageLoad(NormalMode)
      }

      "when on NotificationMoreThanOneSaoPage and the user selected Yes, must go to multiple sao name page" in {
        navigator.nextPage(
          NotificationMoreThanOneSaoPage,
          NormalMode,
          UserAnswers("id").set(NotificationMoreThanOneSaoPage, true).success.value
        ) mustBe notificationRoutes.NotificationMultiSaoLastOfficerNameController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoLastOfficerNameController, must go to more sao submit notification first date page" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerNamePage,
          NormalMode,
          UserAnswers("id").set(NotificationMoreThanOneSaoPage, true).success.value
        ) mustBe notificationRoutes.NotificationMultiSaoLastOfficerStartDateController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoLastOfficerStartDatePage, must go to who was the sao before page" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerStartDatePage,
          NormalMode,
          UserAnswers("id").set(NotificationMultiSaoLastOfficerStartDatePage, LocalDate.of(2026, 5, 1)).success.value
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoPreviousOfficerNamePage, must go to NotificationMultiSaoPreviousOfficerStartDate" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerNamePage(0),
          NormalMode,
          UserAnswers("id")
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerStartDateController.onPageLoad(NormalMode, 0)
      }

      "when on NotificationMultiSaoPreviousOfficerStartDatePage, must go to NotificationMultiSaoPreviousOfficerEndDate page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerStartDatePage(0),
          NormalMode,
          UserAnswers("id")
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerEndDateController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoPreviousOfficerEndDatePage, must go to NotificationMultiSaoAreAllAdded page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerEndDatePage(0),
          NormalMode,
          UserAnswers("id")
        ) mustBe notificationRoutes.NotificationMultiSaoAreAllAddedController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoAreAllAddedPage, and no response is in the database, must throw an exception" in {
        intercept[NotImplementedError] {
          navigator.nextPage(
            NotificationMultiSaoAreAllAddedPage(0),
            NormalMode,
            UserAnswers("id")
          )
        }
      }

      "when on NotificationMultiSaoAreAllAddedPage, and the user answers yes, must go to the notification task list" in {
        navigator.nextPage(
          NotificationMultiSaoAreAllAddedPage(0),
          NormalMode,
          UserAnswers("id").set(NotificationMultiSaoAreAllAddedPage(0), true).success.value
        ) mustBe notificationRoutes.NotificationTaskListController.onPageLoad()
      }

      "when on NotificationMultiSaoAreAllAddedPage, and the user answers no, must go to NotificationMultiSaoPreviousOfficerName page with an incremented saoIndex" in {
        navigator.nextPage(
          NotificationMultiSaoAreAllAddedPage(0),
          NormalMode,
          UserAnswers("id").set(NotificationMultiSaoAreAllAddedPage(0), false).success.value
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode, 1)
      }

      "when on NotificationSingleSaoOfficerNamePage, must go to the submit notification start page" in {
        navigator.nextPage(
          NotificationSingleSaoOfficerNamePage,
          NormalMode,
          UserAnswers("id").set(NotificationSingleSaoOfficerNamePage, "Firstname Lastname").success.value
        ) mustBe notificationRoutes.NotificationTaskListController.onPageLoad()
      }

      "when on UploadTemplateTablePage with no parsing errors, must go to notification start page" in {
        val userAnswers =
          UserAnswers("id")
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
          UserAnswers("id")
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
          UserAnswers("id")
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
          UserAnswers("id")
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "SubmissionTypePage" - {

        "when on SubmissionTypePage and the user chose notification only, must go to NotificationTaskList" in {
          navigator.nextPage(
            SubmissionTypePage,
            NormalMode,
            UserAnswers("id").set(SubmissionTypePage, SubmissionType.Notification).get
          ) mustBe notificationRoutes.NotificationTaskListController.onPageLoad()
        }

        "when on SubmissionTypePage and the user chose certificate only, must go to CertificateTaskList" in {
          navigator.nextPage(
            SubmissionTypePage,
            NormalMode,
            UserAnswers("id").set(SubmissionTypePage, SubmissionType.Certificate).get
          ) mustBe certificateRoutes.CertificateTaskListController.onPageLoad(
            CertificateTaskListStage.ProvideSaoDetailsStage
          )
        }

        "when on SubmissionTypePage and the user chose both the notification and the certificate, must throw an exception" in {
          intercept[NotImplementedError] {
            navigator.nextPage(
              SubmissionTypePage,
              NormalMode,
              UserAnswers("id").set(SubmissionTypePage, SubmissionType.Combined).get
            )
          }
        }
      }

      // certificate flow

      "when on CertificateSaoFullName, must go to CertificateSaoEmail page" in {
        navigator.nextPage(
          CertificateSaoFullNamePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe certificateRoutes.CertificateSaoEmailController.onPageLoad(NormalMode)
      }

      "when on CertificateSaoEmail, must go to CertificateTaskList page" in {
        navigator.nextPage(
          CertificateSaoEmailPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe certificateRoutes.CertificateTaskListController.onPageLoad(
          CertificateTaskListStage.UploadSubmissionTemplateStage
        )
      }

      "when on CertificateUploadTemplateTableErrorPage with no parsing errors, must go to upload form page" in {
        val userAnswers =
          UserAnswers("id")
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
          UserAnswers("id")
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
          UserAnswers("id")
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "when on CertificateReviewQualified, must go to CertificateReviewUnqualified page" in {
        navigator.nextPage(
          CertificateReviewQualifiedPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe certificateRoutes.CertificateReviewUnqualifiedController.onPageLoad()
      }

      "when on CertificateReviewUnqualified, must go to CertificateTaskList page" in {
        navigator.nextPage(
          CertificateReviewUnqualifiedPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe certificateRoutes.CertificateTaskListController.onPageLoad(
          CertificateTaskListStage.SubmitCertificateStage
        )
      }

      "when on CertificateAdditionalInformation, must go to CertificateWhoIsSubmitting page" in {
        navigator.nextPage(
          CertificateAdditionalInformationPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe certificateRoutes.CertificateWhoIsSubmittingController.onPageLoad(NormalMode)
      }

      "when on CertificateWhoIsSubmitting, must go to CertificateDeclarationSao page" in {
        navigator.nextPage(
          CertificateWhoIsSubmittingPage,
          NormalMode,
          UserAnswers("id").set(CertificateWhoIsSubmittingPage, CertificateWhoIsSubmitting.Sao).get
        ) mustBe certificateRoutes.CertificateDeclarationSaoController.onPageLoad(NormalMode)
      }

      "when on CertificateWhoIsSubmitting, must go to CertificateDeclarationStandIn page" in {
        navigator.nextPage(
          CertificateWhoIsSubmittingPage,
          NormalMode,
          UserAnswers("id").set(CertificateWhoIsSubmittingPage, CertificateWhoIsSubmitting.StandIn).get
        ) mustBe certificateRoutes.CertificateDeclarationStandInController.onPageLoad(NormalMode)
      }

      "when on CertificateDeclarationSao, must go to CertificateCheckYourAnswers page" in {
        navigator.nextPage(
          CertificateDeclarationSaoPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
      }

      "when on CertificateDeclarationStandIn, must go to CertificateCheckYourAnswers page" in {
        navigator.nextPage(
          CertificateDeclarationStandInPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
      }

      "when on CertificateConfirmation, must go to CertificateTaskList page" in {
        navigator.nextPage(
          CertificateConfirmationPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe certificateRoutes.CertificateTaskListController.onPageLoad(CertificateTaskListStage.Complete)
      }
    }

    "in Check mode" - {

      "when on NotificationAdditionalInformationPage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationAdditionalInformationPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on SaoNamePage, must go to certificate check your answers page" in {
        navigator.nextPage(
          SaoNamePage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.CombinedCertificateCheckYourAnswersController.onPageLoad()
      }

      "when on CertificateAdditionalInformationPage, must go to certificate check your answers page" in {
        navigator.nextPage(
          CertificateAdditionalInformationPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe certificateRoutes.CertificateCheckYourAnswersController.onPageLoad()
      }

      "when on SaoEmailPage, must go to certificate check your answers page" in {
        navigator.nextPage(
          SaoEmailPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.CombinedCertificateCheckYourAnswersController.onPageLoad()
      }

      "when on SaoEmailCommunicationChoicePage, must go to certificate check your answers page" in {
        navigator.nextPage(
          SaoEmailCommunicationChoicePage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.CombinedCertificateCheckYourAnswersController.onPageLoad()
      }

      "must throw an not-implemented error for an unspecified configuration" in {
        case object UnknownPage extends Page
        intercept[NotImplementedError] {
          navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id"))
        }
      }

    }
  }
}
