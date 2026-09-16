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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.*
import pages.notification.*
import services.csvparser.UploadTemplateCsvSchema.{Column, TemplateError}

import java.time.LocalDate

class NotificationNavigatorSpec extends SpecBase with GuiceOneAppPerSuite {

  lazy val navigator: Navigator = app.injector.instanceOf[NotificationNavigator]

  "NotificationNavigator.nextPage" - {

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

      "when on NotificationConfirmationPage, must go to notification task list" in {
        navigator.nextPage(
          NotificationConfirmationPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationTaskListController.onComplete()
      }

      "when on NotificationMoreThanOneSaoPage and the user selected No, must go to Sao name page" in {
        navigator.nextPage(
          NotificationMoreThanOneSaoPage(NormalMode),
          NormalMode,
          emptyUserAnswers.add(NotificationMoreThanOneSaoPage(NormalMode), false)
        ) mustBe notificationRoutes.NotificationSingleSaoOfficerNameController.onPageLoad(NormalMode)
      }

      "when on NotificationMoreThanOneSaoPage and the user selected Yes, must go to multiple sao name page" in {
        navigator.nextPage(
          NotificationMoreThanOneSaoPage(NormalMode),
          NormalMode,
          emptyUserAnswers.add(NotificationMoreThanOneSaoPage(NormalMode), true)
        ) mustBe notificationRoutes.NotificationMultiSaoLastOfficerNameController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoLastOfficerNameController, must go to more sao submit notification first date page" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerNamePage(NormalMode),
          NormalMode,
          emptyUserAnswers.add(NotificationMoreThanOneSaoPage(NormalMode), true)
        ) mustBe notificationRoutes.NotificationMultiSaoLastOfficerStartDateController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoLastOfficerStartDatePage, must go to who was the sao before page" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerStartDatePage(NormalMode),
          NormalMode,
          emptyUserAnswers
            .add(NotificationMultiSaoLastOfficerStartDatePage(NormalMode), LocalDate.of(2026, 5, 1))
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoPreviousOfficerNamePage, must go to NotificationMultiSaoPreviousOfficerStartDate" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode),
          NormalMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerStartDateController.onPageLoad(NormalMode, 0)
      }

      "when on NotificationMultiSaoPreviousOfficerStartDatePage, must go to NotificationMultiSaoPreviousOfficerEndDate page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode),
          NormalMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerEndDateController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoPreviousOfficerEndDatePage, must go to NotificationMultiSaoAreAllAdded page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode),
          NormalMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationMultiSaoAreAllAddedController.onPageLoad(NormalMode)
      }

      "when on NotificationMultiSaoAreAllAddedPage, and no response is in the database, must throw an exception" in {
        intercept[NotImplementedError] {
          navigator.nextPage(
            NotificationMultiSaoAreAllAddedPage(0, NormalMode),
            NormalMode,
            emptyUserAnswers
          )
        }
      }

      "when on NotificationMultiSaoAreAllAddedPage, and the user answers yes, must go to the notification task list" in {
        navigator.nextPage(
          NotificationMultiSaoAreAllAddedPage(0, NormalMode),
          NormalMode,
          emptyUserAnswers.add(NotificationMultiSaoAreAllAddedPage(0, NormalMode), true)
        ) mustBe notificationRoutes.NotificationTaskListController.onPageLoad()
      }

      "when on NotificationMultiSaoAreAllAddedPage, and the user answers no, must go to NotificationMultiSaoPreviousOfficerName page with an incremented saoIndex" in {
        navigator.nextPage(
          NotificationMultiSaoAreAllAddedPage(0, NormalMode),
          NormalMode,
          emptyUserAnswers.add(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(NormalMode, 1)
      }

      "when on NotificationSingleSaoOfficerNamePage, must go to the submit notification start page" in {
        navigator.nextPage(
          NotificationSingleSaoOfficerNamePage(NormalMode),
          NormalMode,
          emptyUserAnswers.add(NotificationSingleSaoOfficerNamePage(NormalMode), "Firstname Lastname")
        ) mustBe notificationRoutes.NotificationTaskListController.onPageLoad()
      }

      "when on UploadTemplateTablePage with no parsing errors, must go to notification start page" in {
        val userAnswers =
          emptyUserAnswers
            .add(UploadTemplateTablePage, UploadTemplateTableData(rows = Seq.empty, errors = Seq.empty))

        navigator.nextPage(
          UploadTemplateTablePage,
          NormalMode,
          userAnswers
        ) mustBe notificationRoutes.NotificationTaskListController.onPageLoad()
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
        ) mustBe notificationRoutes.NotificationUploadFormController.onPageLoad()
      }

      "when on UploadTemplateTablePage with no upload data, must go to journey recovery page" in {
        navigator.nextPage(
          UploadTemplateTablePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

    }

    "in Check mode" - {

      "when on NotificationSingleSaoOfficerNamePage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationSingleSaoOfficerNamePage(NormalMode),
          CheckMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationMultiSaoLastOfficerNamePage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerNamePage(NormalMode),
          CheckMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationMultiSaoLastOfficerStartDatePage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerStartDatePage(NormalMode),
          CheckMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationMultiSaoPreviousOfficerNamePage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode),
          CheckMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationMultiSaoPreviousOfficerStartDatePage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode),
          CheckMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationMultiSaoPreviousOfficerEndDatePage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode),
          CheckMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationAdditionalInformationPage, must go to notification check your answers page" in {
        navigator.nextPage(
          NotificationAdditionalInformationPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "must throw an not-implemented error for an unspecified configuration" in {
        case object UnknownPage extends Page
        intercept[NotImplementedError] {
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
              ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
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
              ) mustBe notificationRoutes.NotificationSingleSaoOfficerNameController.onPageLoad(TransactionMode)
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
              ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
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
              ) mustBe notificationRoutes.NotificationMultiSaoLastOfficerNameController.onPageLoad(TransactionMode)
            }
          }
        }

        "the user has not previously responded, throw an exception" in {
          val userAnswers = emptyUserAnswers.add(NotificationMoreThanOneSaoPage(TransactionMode), false)

          intercept[NotImplementedError] {
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
        ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
      }

      "when on NotificationMultiSaoLastOfficerNamePage, must go to last officer start date page" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerNamePage(NormalMode),
          TransactionMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationMultiSaoLastOfficerStartDateController.onPageLoad(TransactionMode)
      }

      "when on NotificationMultiSaoLastOfficerStartDatePage, must go to check your answers" in {
        navigator.nextPage(
          NotificationMultiSaoLastOfficerStartDatePage(NormalMode),
          TransactionMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(TransactionMode, 0)
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
            ) mustBe notificationRoutes.NotificationCheckYourAnswersController.onPageLoad()
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
              ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(
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
              ) mustBe notificationRoutes.NotificationCheckYourAnswersController
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
              ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerNameController.onPageLoad(
                TransactionMode,
                2
              )
            }
          }

        }

        "when their is no answer to the question" - {
          "throw an exception" in {
            intercept[NotImplementedError] {
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
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerStartDateController.onPageLoad(
          TransactionMode,
          0
        )
      }

      "when on NotificationMultiSaoPreviousOfficerStartDatePage, must go to NotificationMultiSaoPreviousOfficerEndDate page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode),
          TransactionMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerEndDateController.onPageLoad(TransactionMode)
      }

      "when on NotificationMultiSaoPreviousOfficerEndDatePage, must go to NotificationMultiSaoAreAllAdded page" in {
        navigator.nextPage(
          NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode),
          TransactionMode,
          emptyUserAnswers
        ) mustBe notificationRoutes.NotificationMultiSaoAreAllAddedController.onPageLoad(TransactionMode)
      }

      "must throw an not-implemented error for an unspecified configuration" in {
        case object UnknownPage extends Page
        intercept[NotImplementedError] {
          navigator.nextPage(UnknownPage, TransactionMode, emptyUserAnswers)
        }
      }
    }
  }
}
