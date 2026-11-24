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
import controllers.combined.provideSaoDetails.routes as provideSaoDetailsRoutes
import controllers.combined.routes as combinedRoutes
import controllers.combined.submitNotification.routes as submitNotificationRoutes
import controllers.combined.uploadTemplate.routes as uploadTemplateRoutes
import controllers.routes
import models.*
import models.upload.UploadTemplateTableData
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.*
import pages.certificate.*
import pages.combined.CombinedNotificationConfirmationPage
import pages.notification.*
import play.api.mvc.Call
import services.csvparser.UploadTemplateCsvSchema.{Column, TemplateError}

import java.time.LocalDate

class CombinedNavigatorSpec extends SpecBase with GuiceOneAppPerSuite {

  lazy val navigator: Navigator = app.injector.instanceOf[CombinedNavigator]

  "CombinedNavigator.nextPage" - {

    "in Normal mode" - {

      "must throw an illegal state error for an unspecified configuration" in {
        case object UnknownPage extends Page
        intercept[IllegalStateException] {
          navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers)
        }
      }

      "must throw illegal state error when on NotificationMoreThanOneSaoPage and no answer is selected" in {
        intercept[IllegalStateException] {
          navigator.nextPage(
            NotificationMoreThanOneSaoPage(NormalMode),
            NormalMode,
            emptyUserAnswers
          )
        }
      }

      "must throw illegal state error when on NotificationMultiSaoAreAllAddedPage and no answer is selected" in {
        intercept[IllegalStateException] {
          navigator.nextPage(
            NotificationMultiSaoAreAllAddedPage(0, NormalMode),
            NormalMode,
            emptyUserAnswers
          )
        }
      }

      "must throw illegal state error when on CertificateWhoIsSubmittingPage and no answer is selected" in {
        intercept[IllegalStateException] {
          navigator.nextPage(
            CertificateWhoIsSubmittingPage(NormalMode),
            NormalMode,
            emptyUserAnswers
          )
        }
      }

      "must navigate from NotificationSingleSaoOfficerNamePage to TaskList" in {
        navigator.nextPage(
          NotificationSingleSaoOfficerNamePage(NormalMode),
          NormalMode,
          emptyUserAnswers
        ) mustBe combinedRoutes.CombinedTaskListController.onPageLoad
      }

      "must navigate from NotificationMultiSaoLastOfficerStartDatePage to PreviousOfficerName" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerStartDatePage(NormalMode),
          NormalMode,
          emptyUserAnswers
        ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode)
      }

      "must navigate from NotificationMultiSaoPreviousOfficerNamePage to PreviousOfficerStartDate" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode),
          NormalMode,
          emptyUserAnswers
        ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerStartDateController.onPageLoad(NormalMode)
      }

      "must navigate from NotificationMultiSaoPreviousOfficerStartDatePage to PreviousOfficerEndDate" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode),
          NormalMode,
          emptyUserAnswers
        ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerEndDateController.onPageLoad(NormalMode)
      }

      "must navigate from NotificationMultiSaoPreviousOfficerEndDatePage to AreAllAdded" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode),
          NormalMode,
          emptyUserAnswers
        ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoAreAllAddedController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoLastOfficerNamePage, must go to more sao submit notification first date page" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerNamePage(NormalMode),
          NormalMode,
          emptyUserAnswers.add(NotificationMoreThanOneSaoPage(NormalMode), true)
        ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoLastOfficerStartDateController.onPageLoad(NormalMode)
      }

      "must navigate from NotificationAdditionalInformationPage to ConfirmYourNotification" in {
        navigator.nextPage(
          NotificationAdditionalInformationPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe submitNotificationRoutes.CombinedConfirmYourNotificationController.onPageLoad()
      }

      "must navigate from ConfirmYourNotificationPage to NotificationCheckYourAnswers" in {
        navigator.nextPage(
          ConfirmYourNotificationPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
      }

      "must navigate from CombinedNotificationConfirmationPage to TaskList" in {
        navigator.nextPage(
          CombinedNotificationConfirmationPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe combinedRoutes.CombinedTaskListController.onPageLoad
      }
//
//      "must navigate from CertificateSaoFullNamePage to CertificateSaoEmail" in {
//        navigator.nextPage(
//          CertificateSaoFullNamePage,
//          NormalMode,
//          emptyUserAnswers
//        ) mustBe submitCertificateRoutes.CombinedCertificateSaoEmailController.onPageLoad(NormalMode)
//      }
//
//      "must navigate from CertificateSaoEmailPage to ReviewQualified" in {
//        navigator.nextPage(
//          CertificateSaoEmailPage,
//          NormalMode,
//          emptyUserAnswers
//        ) mustBe submitCertificateRoutes.CombinedCertificateReviewQualifiedController.onPageLoad(page = 0)
//      }
//
//      "must navigate from CertificateReviewQualifiedPage to ReviewUnqualified" in {
//        navigator.nextPage(
//          CertificateReviewQualifiedPage,
//          NormalMode,
//          emptyUserAnswers
//        ) mustBe submitCertificateRoutes.CombinedCertificateReviewUnqualifiedController.onPageLoad(page = 0)
//      }
//
//      "must navigate from CertificateReviewUnqualifiedPage to AdditionalInformation" in {
//        navigator.nextPage(
//          CertificateReviewUnqualifiedPage,
//          NormalMode,
//          emptyUserAnswers
//        ) mustBe submitCertificateRoutes.CombinedCertificateAdditionalInformationController.onPageLoad(NormalMode)
//      }
//
//      "must navigate from CertificateAdditionalInformationPage to WhoIsSubmitting" in {
//        navigator.nextPage(
//          CertificateAdditionalInformationPage,
//          NormalMode,
//          emptyUserAnswers
//        ) mustBe submitCertificateRoutes.CombinedCertificateWhoIsSubmittingController.onPageLoad(NormalMode)
//      }
//
//      "must navigate from CertificateDeclarationSaoPage to CertificateCheckYourAnswers" in {
//        navigator.nextPage(
//          CertificateDeclarationSaoPage(NormalMode),
//          NormalMode,
//          emptyUserAnswers
//        ) mustBe submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onPageLoad()
//      }
//
//      "must navigate from CertificateDeclarationStandInPage to CertificateCheckYourAnswers" in {
//        navigator.nextPage(
//          CertificateDeclarationStandInPage(NormalMode),
//          NormalMode,
//          emptyUserAnswers
//        ) mustBe submitCertificateRoutes.CombinedCertificateCheckYourAnswersController.onPageLoad()
//      }

//      "must navigate from CombinedCertificateConfirmationPage to Complete" in {
//        navigator.nextPage(
//          CombinedCertificateConfirmationPage,
//          NormalMode,
//          emptyUserAnswers
//        ) mustBe combinedRoutes.CombinedTaskListController.onComplete
//      }

      "when on NotificationMoreThanOneSaoPage and the user selected No, must go to Sao name page" in {
        navigator.nextPage(
          NotificationMoreThanOneSaoPage(NormalMode),
          NormalMode,
          emptyUserAnswers.add(NotificationMoreThanOneSaoPage(NormalMode), false)
        ) mustBe provideSaoDetailsRoutes.CombinedSingleSaoOfficerNameController.onPageLoad(NormalMode)
      }

      "when on NotificationMoreThanOneSaoPage and the user selected Yes, must go to multiple sao name page" in {
        navigator.nextPage(
          NotificationMoreThanOneSaoPage(NormalMode),
          NormalMode,
          emptyUserAnswers.add(NotificationMoreThanOneSaoPage(NormalMode), true)
        ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoLastOfficerNameController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoAreAllAddedPage and the user selected Yes, must go to combined task list page" in {
        navigator.nextPage(
          NotificationMultiSaoAreAllAddedPage(0, NormalMode),
          NormalMode,
          emptyUserAnswers.add(NotificationMultiSaoAreAllAddedPage(0, NormalMode), true)
        ) mustBe combinedRoutes.CombinedTaskListController.onPageLoad
      }

      "when on NotificationMultiSaoAreAllAddedPage and the user selected No, must go to the multi sao previous officer name page" in {
        navigator.nextPage(
          NotificationMultiSaoAreAllAddedPage(0, NormalMode),
          NormalMode,
          emptyUserAnswers.add(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
        ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode, 1)
      }

      "when on UploadTemplateTablePage with no parsing errors, must go to combined start page" in {
        val userAnswers =
          emptyUserAnswers
            .add(UploadTemplateTablePage, UploadTemplateTableData(rows = Seq.empty, errors = Seq.empty))

        navigator.nextPage(
          UploadTemplateTablePage,
          NormalMode,
          userAnswers
        ) mustBe combinedRoutes.CombinedTaskListController.onPageLoad
      }

      "when on UploadTemplateTablePage with parsing errors, must go to upload form page" in {
        val userAnswers =
          emptyUserAnswers
            .add(
              UploadTemplateTablePage,
              UploadTemplateTableData(
                rows = Seq.empty,
                errors = Seq(models.upload.TemplateParseError(9, Some(Column.Utr), TemplateError.UtrError))
              )
            )

        navigator.nextPage(
          UploadTemplateTablePage,
          NormalMode,
          userAnswers
        ) mustBe uploadTemplateRoutes.CombinedUploadFormController.onPageLoad()
      }

      "when on UploadTemplateTablePage with no upload data, must go to journey recovery page" in {
        navigator.nextPage(
          UploadTemplateTablePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }
//
//      "when on CertificateWhoIsSubmitting, must go to CertificateDeclarationSao page" in {
//        navigator.nextPage(
//          CertificateWhoIsSubmittingPage(NormalMode),
//          NormalMode,
//          emptyUserAnswers.set(CertificateWhoIsSubmittingPage(NormalMode), CertificateWhoIsSubmitting.Sao).get
//        ) mustBe submitCertificateRoutes.CombinedCertificateDeclarationSaoController.onPageLoad(NormalMode)
//      }
//
//      "when on CertificateWhoIsSubmitting, must go to CertificateDeclarationStandIn page" in {
//        navigator.nextPage(
//          CertificateWhoIsSubmittingPage(NormalMode),
//          NormalMode,
//          emptyUserAnswers.set(CertificateWhoIsSubmittingPage(NormalMode), CertificateWhoIsSubmitting.StandIn).get
//        ) mustBe submitCertificateRoutes.CombinedCertificateDeclarationStandInController.onPageLoad(NormalMode)
//      }
    }

    "in Check mode" - {

      "when on NotificationSingleSaoOfficerNamePage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationSingleSaoOfficerNamePage(NormalMode),
          CheckMode,
          emptyUserAnswers
        ) mustBe submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationMultiSaoLastOfficerNamePage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerNamePage(NormalMode),
          CheckMode,
          emptyUserAnswers
        ) mustBe submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationMultiSaoLastOfficerStartDatePage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerStartDatePage(NormalMode),
          CheckMode,
          emptyUserAnswers
        ) mustBe submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationMultiSaoPreviousOfficerNamePage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode),
          CheckMode,
          emptyUserAnswers
        ) mustBe submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationMultiSaoPreviousOfficerStartDatePage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode),
          CheckMode,
          emptyUserAnswers
        ) mustBe submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationMultiSaoPreviousOfficerEndDatePage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode),
          CheckMode,
          emptyUserAnswers
        ) mustBe submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationAdditionalInformationPage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationAdditionalInformationPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
      }

      "must throw an illegal state error for an unspecified configuration" in {
        case object UnknownPage extends Page
        intercept[IllegalStateException] {
          navigator.nextPage(UnknownPage, CheckMode, emptyUserAnswers)
        }
      }
    }
    "in Transaction mode" - {
      "on NotificationMoreThanOneSaoPage" - {
        "the user responds no meaning there is only one SAO" - {
          "user answers contains previous answers for the single SAO route" - {
            "we are redirected to the notification check your answers page" in {
              val userAnswers = emptyUserAnswers
                .add(NotificationMoreThanOneSaoPage(NormalMode), false)
                .add(NotificationSingleSaoOfficerNamePage(NormalMode), "Firstname Lastname")
                .add(NotificationMoreThanOneSaoPage(TransactionMode), false)

              navigator.nextPage(
                NotificationMoreThanOneSaoPage(TransactionMode),
                TransactionMode,
                userAnswers
              ) mustBe submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
            }
          }

          "user answers does not contain previous answers for the single SAO route" - {
            "we are redirected to the single sao name page in add sao mode" in {
              val userAnswers = emptyUserAnswers
                .add(NotificationMoreThanOneSaoPage(NormalMode), true)
                .add(NotificationMoreThanOneSaoPage(TransactionMode), false)

              navigator.nextPage(
                NotificationMoreThanOneSaoPage(NormalMode),
                TransactionMode,
                userAnswers
              ) mustBe provideSaoDetailsRoutes.CombinedSingleSaoOfficerNameController.onPageLoad(TransactionMode)
            }
          }
        }

        "the user responds yes meaning there are multiple SAOs" - {
          "user answers contains previous answers for the multiple SAO route" - {
            "we are redirected to the notification check your answers page" in {
              val userAnswers = emptyUserAnswers
                .add(NotificationMoreThanOneSaoPage(NormalMode), true)
                .add(NotificationMultiSaoLastOfficerNamePage(NormalMode), "Firstname Lastname")
                .add(NotificationMultiSaoLastOfficerStartDatePage(NormalMode), LocalDate.now())
                .add(NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode), "Firstname Lastname II")
                .add(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), LocalDate.now())
                .add(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), LocalDate.now())
                .add(NotificationMultiSaoAreAllAddedPage(0, NormalMode), true)
                .add(NotificationMoreThanOneSaoPage(TransactionMode), true)

              navigator.nextPage(
                NotificationMoreThanOneSaoPage(NormalMode),
                TransactionMode,
                userAnswers
              ) mustBe submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
            }
          }

          "user answers does not contain answers for the multiple SAO route" - {
            "we are redirected to the NotificationMultiSaoLastOfficerNameController page in add sao mode" in {
              val userAnswers = emptyUserAnswers
                .add(NotificationMoreThanOneSaoPage(TransactionMode), true)

              navigator.nextPage(
                NotificationMoreThanOneSaoPage(NormalMode),
                TransactionMode,
                userAnswers
              ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoLastOfficerNameController.onPageLoad(TransactionMode)
            }
          }
        }

        "the user has not previously responded, throw an exception" in {
          val userAnswers = emptyUserAnswers.add(NotificationMoreThanOneSaoPage(TransactionMode), false)

          intercept[IllegalStateException] {
            navigator.nextPage(
              NotificationMultiSaoAreAllAddedPage(0, NormalMode),
              TransactionMode,
              userAnswers
            )
          }
        }
      }

      "when on NotificationSingleSaoOfficerNamePage, must go to check your answers" in {
        navigator.nextPage(
          NotificationSingleSaoOfficerNamePage(NormalMode),
          TransactionMode,
          emptyUserAnswers
        ) mustBe submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationMultiSaoLastOfficerNamePage, must go to last officer start date page" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerNamePage(NormalMode),
          TransactionMode,
          emptyUserAnswers
        ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoLastOfficerStartDateController.onPageLoad(TransactionMode)
      }

      "when on NotificationMultiSaoLastOfficerStartDatePage, must go to check your answers" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerStartDatePage(NormalMode),
          TransactionMode,
          emptyUserAnswers
        ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerNameController.onPageLoad(TransactionMode, 0)
      }

      "when on NotificationMultiSaoAreAllAddedPage" - {
        "when user answers yes" - {
          "go to notification check your answers page" in {
            val userAnswers = emptyUserAnswers
              .add(NotificationMultiSaoAreAllAddedPage(0, TransactionMode), false)
              .add(NotificationMultiSaoAreAllAddedPage(1, TransactionMode), true)

            navigator.nextPage(
              NotificationMultiSaoAreAllAddedPage(1, TransactionMode),
              TransactionMode,
              userAnswers
            ) mustBe submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad()
          }
        }

        "when user answers no" - {
          "when the users prior answer was yes" - {
            "go to previous sao name page at the right index" in {
              val userAnswers = emptyUserAnswers
                .add(NotificationMultiSaoAreAllAddedPage(0, TransactionMode), false)
                .add(NotificationMultiSaoAreAllAddedPage(1, TransactionMode), false)
                .add(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
                .add(NotificationMultiSaoAreAllAddedPage(1, NormalMode), true)

              navigator.nextPage(
                NotificationMultiSaoAreAllAddedPage(1, TransactionMode),
                TransactionMode,
                userAnswers
              ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerNameController.onPageLoad(
                TransactionMode,
                2
              )
            }
          }

          "when the users prior answer was no" - {
            "go to check your answers page" in {
              val userAnswers = emptyUserAnswers
                .add(NotificationMultiSaoAreAllAddedPage(0, TransactionMode), false)
                .add(NotificationMultiSaoAreAllAddedPage(1, TransactionMode), false)
                .add(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
                .add(NotificationMultiSaoAreAllAddedPage(1, NormalMode), false)

              navigator.nextPage(
                NotificationMultiSaoAreAllAddedPage(1, TransactionMode),
                TransactionMode,
                userAnswers
              ) mustBe submitNotificationRoutes.CombinedNotificationCheckYourAnswersController
                .onPageLoad()
            }
          }

          "when the user has no prior answer" - {
            "throw an exception" in {
              val userAnswers = emptyUserAnswers
                .add(NotificationMultiSaoAreAllAddedPage(0, TransactionMode), false)
                .add(NotificationMultiSaoAreAllAddedPage(1, TransactionMode), false)

              navigator.nextPage(
                NotificationMultiSaoAreAllAddedPage(1, TransactionMode),
                TransactionMode,
                userAnswers
              ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerNameController.onPageLoad(
                TransactionMode,
                2
              )
            }
          }

        }

        "when their is no answer to the question" - {
          "throw an exception" in {
            intercept[IllegalStateException] {
              navigator.nextPage(
                NotificationMultiSaoAreAllAddedPage(0, TransactionMode),
                TransactionMode,
                emptyUserAnswers
              )
            }
          }
        }
      }

      "when on NotificationMultiSaoPreviousOfficerNamePage, must go to NotificationMultiSaoPreviousOfficerStartDate" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode),
          TransactionMode,
          emptyUserAnswers
        ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerStartDateController.onPageLoad(
          TransactionMode,
          0
        )
      }

      "when on NotificationMultiSaoPreviousOfficerStartDatePage, must go to NotificationMultiSaoPreviousOfficerEndDate page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode),
          TransactionMode,
          emptyUserAnswers
        ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoPreviousOfficerEndDateController.onPageLoad(TransactionMode)
      }

      "when on NotificationMultiSaoPreviousOfficerEndDatePage, must go to NotificationMultiSaoAreAllAdded page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode),
          TransactionMode,
          emptyUserAnswers
        ) mustBe provideSaoDetailsRoutes.CombinedMultiSaoAreAllAddedController.onPageLoad(TransactionMode)
      }

      "must throw an not-implemented error for an unspecified configuration" in {
        case object UnknownPage extends Page
        intercept[IllegalStateException] {
          navigator.nextPage(UnknownPage, TransactionMode, emptyUserAnswers)
        }
      }
    }

  }
}
