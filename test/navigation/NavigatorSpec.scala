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
import controllers.notification.routes as notificationRoutes
import controllers.routes
import models.*
import models.upload.UploadTemplateTableData
import pages.*
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
        ) mustBe routes.QualifiedCompaniesController.onPageLoad()
      }

      "when on QualifiedCompaniesPage, must go to unqualified companies page" in {
        navigator.nextPage(
          QualifiedCompaniesPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.UnqualifiedCompaniesController.onPageLoad()
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
        ) mustBe notificationRoutes.OneSaoSubmitNotificationFullNameController.onPageLoad(NormalMode)
      }

      "when on NotificationMoreThanOneSaoPage and the user selected Yes, must go to multiple sao name page" in {
        navigator.nextPage(
          NotificationMoreThanOneSaoPage,
          NormalMode,
          UserAnswers("id").set(NotificationMoreThanOneSaoPage, true).success.value
        ) mustBe notificationRoutes.MoreSaoSubmitNotificationFullNameController.onPageLoad(NormalMode)
      }

      "when on MoreSaoSubmitNotificationFullNameController, must go to more sao submit notification first date page" in {
        navigator.nextPage(
          MoreSaoSubmitNotificationFullNamePage,
          NormalMode,
          UserAnswers("id").set(NotificationMoreThanOneSaoPage, true).success.value
        ) mustBe notificationRoutes.NotificationMoreSaoFirstStartDateController.onPageLoad(NormalMode)
      }

      "when on NotificationMoreSaoFirstStartDatePage, must go to who was the sao before page" in {
        navigator.nextPage(
          NotificationMoreSaoFirstStartDatePage,
          NormalMode,
          UserAnswers("id").set(NotificationMoreSaoFirstStartDatePage, LocalDate.of(2026, 5, 1)).success.value
        ) mustBe notificationRoutes.NotificationMultiSaoLastOfficerNameController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoLastOfficerNamePage, must go to NotificationMoreSaoSecondStartDate" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerNamePage(0),
          NormalMode,
          UserAnswers("id")
        ) mustBe notificationRoutes.NotificationMoreSaoSecondStartDateController.onPageLoad(NormalMode, 0)
      }

      "when on NotificationMoreSaoSecondStartDatePage, must go to NotificationMoreSaoSecondEndDate page" in {
        navigator.nextPage(
          NotificationMoreSaoSecondStartDatePage(0),
          NormalMode,
          UserAnswers("id")
        ) mustBe notificationRoutes.NotificationMoreSaoSecondEndDateController.onPageLoad(NormalMode)
      }

      "when on NotificationMoreSaoSecondEndDatePage, must go to NotificationMoreSaoAreAllAdded page" in {
        navigator.nextPage(
          NotificationMoreSaoSecondEndDatePage(0),
          NormalMode,
          UserAnswers("id")
        ) mustBe notificationRoutes.NotificationMoreSaoAreAllAddedController.onPageLoad(NormalMode)
      }

      "when on NotificationMoreSaoAreAllAddedPage, and no response is in the database, must throw an exception" in {
        intercept[NotImplementedError] {
          navigator.nextPage(
            NotificationMoreSaoAreAllAddedPage(0),
            NormalMode,
            UserAnswers("id")
          )
        }
      }

      "when on NotificationMoreSaoAreAllAddedPage, and the user answers yes, must go to the notification task list" in {
        navigator.nextPage(
          NotificationMoreSaoAreAllAddedPage(0),
          NormalMode,
          UserAnswers("id").set(NotificationMoreSaoAreAllAddedPage(0), true).success.value
        ) mustBe notificationRoutes.NotificationTaskListController.onPageLoad()
      }

      "when on NotificationMoreSaoAreAllAddedPage, and the user answers no, must go to NotificationMultiSaoLastOfficerName page with an incremented saoIndex" in {
        navigator.nextPage(
          NotificationMoreSaoAreAllAddedPage(0),
          NormalMode,
          UserAnswers("id").set(NotificationMoreSaoAreAllAddedPage(0), false).success.value
        ) mustBe notificationRoutes.NotificationMultiSaoLastOfficerNameController.onPageLoad(NormalMode, 1)
      }

      "when on OneSaoSubmitNotificationFullNamePage, must go to the submit notification start page" in {
        navigator.nextPage(
          OneSaoSubmitNotificationFullNamePage,
          NormalMode,
          UserAnswers("id").set(OneSaoSubmitNotificationFullNamePage, "Firstname Lastname").success.value
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
          ) mustBe routes.CertificateTaskListController.onPageLoad(CertificateTaskListStage.ProvideSaoDetailsStage)
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
        ) mustBe routes.CertificateSaoEmailController.onPageLoad(NormalMode)
      }

      "when on CertificateSaoEmail, must go to CertificateTaskList page" in {
        navigator.nextPage(
          CertificateSaoEmailPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.CertificateTaskListController.onPageLoad(
          CertificateTaskListStage.UploadSubmissionTemplateStage
        )
      }

      "when on CertificateReviewQualified, must go to CertificateReviewUnqualified page" in {
        navigator.nextPage(
          CertificateReviewQualifiedPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.CertificateReviewUnqualifiedController.onPageLoad()
      }

      "when on CertificateReviewUnqualified, must go to CertificateTaskList page" in {
        navigator.nextPage(
          CertificateReviewUnqualifiedPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.CertificateTaskListController.onPageLoad(CertificateTaskListStage.SubmitCertificateStage)
      }

      "when on CertificateAdditionalInformation, must go to CertificateWhoIsSubmitting page" in {
        navigator.nextPage(
          CertificateAdditionalInformationPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.CertificateWhoIsSubmittingController.onPageLoad(NormalMode)
      }

      "when on CertificateWhoIsSubmitting, must go to CertificateDeclarationSao page" in {
        navigator.nextPage(
          CertificateWhoIsSubmittingPage,
          NormalMode,
          UserAnswers("id").set(CertificateWhoIsSubmittingPage, CertificateWhoIsSubmitting.Sao).get
        ) mustBe routes.CertificateDeclarationSaoController.onPageLoad(NormalMode)
      }

      "when on CertificateWhoIsSubmitting, must go to CertificateDeclarationStandIn page" in {
        navigator.nextPage(
          CertificateWhoIsSubmittingPage,
          NormalMode,
          UserAnswers("id").set(CertificateWhoIsSubmittingPage, CertificateWhoIsSubmitting.StandIn).get
        ) mustBe routes.CertificateDeclarationStandInController.onPageLoad(NormalMode)
      }

      "when on CertificateDeclarationSao, must go to CertificateCheckYourAnswers page" in {
        navigator.nextPage(
          CertificateDeclarationSaoPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.CertificateCheckYourAnswersController.onPageLoad()
      }

      "when on CertificateDeclarationStandIn, must go to CertificateCheckYourAnswers page" in {
        navigator.nextPage(
          CertificateDeclarationStandInPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.CertificateCheckYourAnswersController.onPageLoad()
      }

      "when on CertificateCheckYourAnswers, must go to CertificateConfirmation page" in {
        navigator.nextPage(
          CertificateCheckYourAnswersPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.CertificateConfirmationController.onPageLoad()
      }

      "when on CertificateConfirmation, must go to CertificateTaskList page" in {
        navigator.nextPage(
          CertificateConfirmationPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.CertificateTaskListController.onPageLoad(CertificateTaskListStage.Complete)
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
