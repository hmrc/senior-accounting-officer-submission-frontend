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
import controllers.routes
import models.upload.UploadTemplateTableData
import models.{CheckMode, NormalMode, UserAnswers}
import pages.*

import java.time.LocalDate

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

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
        ) mustBe routes.ConfirmYourNotificationController.onPageLoad()
      }

      "when on ConfirmYourNotificationPage, must go to check your answers page" in {
        navigator.nextPage(
          ConfirmYourNotificationPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationCheckYourAnswersPage, must go to notification confirmation page" in {
        navigator.nextPage(
          NotificationCheckYourAnswersPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.NotificationConfirmationController.onPageLoad()
      }

      "when on ConfirmYourNotificationPage, must go to notification check your answers page" in {
        navigator.nextPage(
          ConfirmYourNotificationPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on SubmitCertificateStartPage, must go to is this SAO on certificate page" in {
        navigator.nextPage(
          SubmitCertificateStartPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.IsThisTheSaoOnCertificateController.onPageLoad(NormalMode)
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
        ) mustBe routes.JointCertificateCheckYourAnswersController.onPageLoad()
      }

      "when on JointCertificateCheckYourAnswersPage, must go to who submits certificate page" in {
        navigator.nextPage(
          JointCertificateCheckYourAnswersPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.JointWhoSubmitsCertificateController.onPageLoad(NormalMode)
      }

      "when on JointWhoSubmitsCertificatePage, must go to qualified companies page" in {
        navigator.nextPage(
          JointWhoSubmitsCertificatePage,
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
        ) mustBe routes.JointCertificateSubmissionDeclarationController.onPageLoad(NormalMode)
      }

      "when on JointCertificateSubmissionDeclarationPage, must go to certificate confirmation page" in {
        navigator.nextPage(
          JointCertificateSubmissionDeclarationPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.CertificateConfirmationController.onPageLoad()
      }

      "when on NotificationConfirmationPage, must go to notification task list" in {
        navigator.nextPage(
          NotificationConfirmationPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.SubmitNotificationStartController.onPageLoad()
      }

      "when on NotificationMoreThanOneSaoPage and the user selected No, must go to Sao name page" in {
        navigator.nextPage(
          NotificationMoreThanOneSaoPage,
          NormalMode,
          UserAnswers("id").set(NotificationMoreThanOneSaoPage, false).success.value
        ) mustBe routes.OneSaoSubmitNotificationFullNameController.onPageLoad(NormalMode)
      }

      "when on NotificationMoreThanOneSaoPage and the user selected Yes, must go to multiple sao name page" in {
        navigator.nextPage(
          NotificationMoreThanOneSaoPage,
          NormalMode,
          UserAnswers("id").set(NotificationMoreThanOneSaoPage, true).success.value
        ) mustBe routes.MoreSaoSubmitNotificationFullNameController.onPageLoad(NormalMode)
      }

      "when on MoreSaoSubmitNotificationFullNameController, must go to more sao submit notification first date page" in {
        navigator.nextPage(
          MoreSaoSubmitNotificationFullNamePage,
          NormalMode,
          UserAnswers("id").set(NotificationMoreThanOneSaoPage, true).success.value
        ) mustBe routes.NotificationMoreSaoFirstStartDateController.onPageLoad(NormalMode)
      }

      "when on NotificationMoreSaoFirstStartDatePage, must go to who was the sao before page" in {
        navigator.nextPage(
          NotificationMoreSaoFirstStartDatePage,
          NormalMode,
          UserAnswers("id").set(NotificationMoreSaoFirstStartDatePage, LocalDate.of(2026, 5, 1)).success.value
        ) mustBe routes.WhoWasTheSaoBeforeController.onPageLoad(NormalMode)
      }

      "when on WhoWasTheSaoBeforePage, must go to NotificationMoreSaoSecondStartDate" in {
        navigator.nextPage(
          WhoWasTheSaoBeforePage(0),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.NotificationMoreSaoSecondStartDateController.onPageLoad(NormalMode, 0)
      }

      "when on NotificationMoreSaoSecondStartDatePage, must go to NotificationMoreSaoSecondEndDate page" in {
        navigator.nextPage(
          NotificationMoreSaoSecondStartDatePage(0),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.NotificationMoreSaoSecondEndDateController.onPageLoad(NormalMode)
      }

      "when on NotificationMoreSaoSecondEndDatePage, must go to NotificationMoreSaoAreAllAdded page" in {
        navigator.nextPage(
          NotificationMoreSaoSecondEndDatePage(0),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.NotificationMoreSaoAreAllAddedController.onPageLoad(NormalMode)
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
        ) mustBe routes.SubmitNotificationStartController.onPageLoad()
      }

      "when on NotificationMoreSaoAreAllAddedPage, and the user answers no, must go to WhoWasTheSaoBefore page with an incremented saoIndex" in {
        navigator.nextPage(
          NotificationMoreSaoAreAllAddedPage(0),
          NormalMode,
          UserAnswers("id").set(NotificationMoreSaoAreAllAddedPage(0), false).success.value
        ) mustBe routes.WhoWasTheSaoBeforeController.onPageLoad(NormalMode, 1)
      }

      "when on OneSaoSubmitNotificationFullNamePage, must go to the submit notification start page" in {
        navigator.nextPage(
          OneSaoSubmitNotificationFullNamePage,
          NormalMode,
          UserAnswers("id").set(OneSaoSubmitNotificationFullNamePage, "Firstname Lastname").success.value
        ) mustBe routes.SubmitNotificationStartController.onPageLoad()
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
        ) mustBe routes.SubmitNotificationStartController.onPageLoad()
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
        ) mustBe routes.NotificationUploadFormController.onPageLoad()
      }

      "when on UploadTemplateTablePage with no upload data, must go to journey recovery page" in {
        navigator.nextPage(
          UploadTemplateTablePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check mode" - {

      "when on NotificationAdditionalInformationPage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationAdditionalInformationPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on SaoNamePage, must go to certificate check your answers page" in {
        navigator.nextPage(
          SaoNamePage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.JointCertificateCheckYourAnswersController.onPageLoad()
      }

      "when on SaoEmailPage, must go to certificate check your answers page" in {
        navigator.nextPage(
          SaoEmailPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.JointCertificateCheckYourAnswersController.onPageLoad()
      }

      "when on SaoEmailCommunicationChoicePage, must go to certificate check your answers page" in {
        navigator.nextPage(
          SaoEmailCommunicationChoicePage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.JointCertificateCheckYourAnswersController.onPageLoad()
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
