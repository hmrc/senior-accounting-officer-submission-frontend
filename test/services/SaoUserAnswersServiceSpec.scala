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
import models.TransactionMode
import models.UserAnswers
import pages.notification.*
import play.api.libs.json.Writes

import java.time.LocalDate

import SaoUserAnswersServiceSpec.*

class SaoUserAnswersServiceSpec extends SpecBase {

  def SUT = new SaoUserAnswersService

  "sanitiseUserAnswers" - {
    "user has provided details for a single sao" - {
      "multi sao user answers are pruned" in {
        val input = UserAnswers("test")
          .add(NotificationMoreThanOneSaoPage(NormalMode), false)
          .add(NotificationSingleSaoOfficerNamePage(NormalMode), singleOfficerName)
          .add(NotificationMultiSaoLastOfficerNamePage(NormalMode), lastOfficerName)
          .add(NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode), previousOfficer1Name)
          .add(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), previousOfficer1StartDate)
          .add(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), previousOfficer1EndDate)
          .add(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
          .add(NotificationMultiSaoPreviousOfficerNamePage(1, NormalMode), previousOfficer2Name)
          .add(NotificationMultiSaoPreviousOfficerStartDatePage(1, NormalMode), previousOfficer2StartDate)
          .add(NotificationMultiSaoPreviousOfficerEndDatePage(1, NormalMode), previousOfficer2EndDate)
          .add(NotificationMultiSaoAreAllAddedPage(1, NormalMode), false)
          .add(NotificationMultiSaoPreviousOfficerNamePage(2, NormalMode), previousOfficer3Name)
          .add(NotificationMultiSaoPreviousOfficerStartDatePage(2, NormalMode), previousOfficer3StartDate)
          .add(NotificationMultiSaoPreviousOfficerEndDatePage(2, NormalMode), previousOfficer3EndDate)
          .add(NotificationMultiSaoAreAllAddedPage(2, NormalMode), true)

        val expected = UserAnswers("test")
          .add(NotificationMoreThanOneSaoPage(NormalMode), false)
          .add(NotificationSingleSaoOfficerNamePage(NormalMode), singleOfficerName)
          .add(NotificationMoreThanOneSaoPage(TransactionMode), false)
          .add(NotificationSingleSaoOfficerNamePage(TransactionMode), singleOfficerName)

        val result = SUT.sanitiseUserAnswers(input)
        result.data mustBe expected.data
      }
    }

    "user has provided details for multiple saos" - {
      "single sao user answers are pruned, last complete sao is marked as the final sao and incomplete sao data is removed" in {
        val input = UserAnswers("test")
          .add(NotificationMoreThanOneSaoPage(NormalMode), true)
          .add(NotificationSingleSaoOfficerNamePage(NormalMode), singleOfficerName)
          .add(NotificationMultiSaoLastOfficerNamePage(NormalMode), lastOfficerName)
          .add(NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode), previousOfficer1Name)
          .add(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), previousOfficer1StartDate)
          .add(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), previousOfficer1EndDate)
          .add(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
          .add(NotificationMultiSaoPreviousOfficerNamePage(1, NormalMode), previousOfficer2Name)
          .add(NotificationMultiSaoPreviousOfficerStartDatePage(1, NormalMode), previousOfficer2StartDate)
          .add(NotificationMultiSaoPreviousOfficerEndDatePage(1, NormalMode), previousOfficer2EndDate)
          .add(NotificationMultiSaoAreAllAddedPage(1, NormalMode), false)
          .add(NotificationMultiSaoPreviousOfficerNamePage(2, NormalMode), previousOfficer3Name)
          .add(NotificationMultiSaoPreviousOfficerStartDatePage(2, NormalMode), previousOfficer3StartDate)
          .add(NotificationMultiSaoPreviousOfficerEndDatePage(2, NormalMode), previousOfficer3EndDate)
          .add(NotificationMultiSaoAreAllAddedPage(2, NormalMode), false)
          .add(NotificationMultiSaoPreviousOfficerNamePage(3, NormalMode), previousOfficer4Name)

        val expected = UserAnswers("test")
          .add(NotificationMoreThanOneSaoPage(NormalMode), true)
          .add(NotificationMultiSaoLastOfficerNamePage(NormalMode), lastOfficerName)
          .add(NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode), previousOfficer1Name)
          .add(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), previousOfficer1StartDate)
          .add(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), previousOfficer1EndDate)
          .add(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
          .add(NotificationMultiSaoPreviousOfficerNamePage(1, NormalMode), previousOfficer2Name)
          .add(NotificationMultiSaoPreviousOfficerStartDatePage(1, NormalMode), previousOfficer2StartDate)
          .add(NotificationMultiSaoPreviousOfficerEndDatePage(1, NormalMode), previousOfficer2EndDate)
          .add(NotificationMultiSaoAreAllAddedPage(1, NormalMode), false)
          .add(NotificationMultiSaoPreviousOfficerNamePage(2, NormalMode), previousOfficer3Name)
          .add(NotificationMultiSaoPreviousOfficerStartDatePage(2, NormalMode), previousOfficer3StartDate)
          .add(NotificationMultiSaoPreviousOfficerEndDatePage(2, NormalMode), previousOfficer3EndDate)
          .add(NotificationMultiSaoAreAllAddedPage(2, NormalMode), true)
          .add(NotificationMoreThanOneSaoPage(TransactionMode), true)
          .add(NotificationMultiSaoLastOfficerNamePage(TransactionMode), lastOfficerName)
          .add(NotificationMultiSaoPreviousOfficerNamePage(0, TransactionMode), previousOfficer1Name)
          .add(NotificationMultiSaoPreviousOfficerStartDatePage(0, TransactionMode), previousOfficer1StartDate)
          .add(NotificationMultiSaoPreviousOfficerEndDatePage(0, TransactionMode), previousOfficer1EndDate)
          .add(NotificationMultiSaoAreAllAddedPage(0, TransactionMode), false)
          .add(NotificationMultiSaoPreviousOfficerNamePage(1, TransactionMode), previousOfficer2Name)
          .add(NotificationMultiSaoPreviousOfficerStartDatePage(1, TransactionMode), previousOfficer2StartDate)
          .add(NotificationMultiSaoPreviousOfficerEndDatePage(1, TransactionMode), previousOfficer2EndDate)
          .add(NotificationMultiSaoAreAllAddedPage(1, TransactionMode), false)
          .add(NotificationMultiSaoPreviousOfficerNamePage(2, TransactionMode), previousOfficer3Name)
          .add(NotificationMultiSaoPreviousOfficerStartDatePage(2, TransactionMode), previousOfficer3StartDate)
          .add(NotificationMultiSaoPreviousOfficerEndDatePage(2, TransactionMode), previousOfficer3EndDate)
          .add(NotificationMultiSaoAreAllAddedPage(2, TransactionMode), true)

        val result = SUT.sanitiseUserAnswers(input)
        result.data mustBe expected.data
      }
    }
  }

  "commitTransaction" - {
    "commiting a single sao transaction" in {
      val input = UserAnswers("test")
        .add(NotificationMoreThanOneSaoPage(TransactionMode), false)
        .add(NotificationSingleSaoOfficerNamePage(TransactionMode), singleOfficerName)

      val expected = UserAnswers("test")
        .add(NotificationMoreThanOneSaoPage(TransactionMode), false)
        .add(NotificationSingleSaoOfficerNamePage(TransactionMode), singleOfficerName)
        .add(NotificationMoreThanOneSaoPage(NormalMode), false)
        .add(NotificationSingleSaoOfficerNamePage(NormalMode), singleOfficerName)

      val result = SUT.commitTransaction(input)
      result.data mustBe expected.data
    }
    "commiting a multi sao transaction" in {
      val input = UserAnswers("test")
        .add(NotificationMoreThanOneSaoPage(TransactionMode), true)
        .add(NotificationMultiSaoLastOfficerNamePage(TransactionMode), lastOfficerName)
        .add(NotificationMultiSaoLastOfficerStartDatePage(TransactionMode), lastOfficerStartDate)
        .add(NotificationMultiSaoPreviousOfficerNamePage(0, TransactionMode), previousOfficer1Name)
        .add(NotificationMultiSaoPreviousOfficerStartDatePage(0, TransactionMode), previousOfficer1StartDate)
        .add(NotificationMultiSaoPreviousOfficerEndDatePage(0, TransactionMode), previousOfficer1EndDate)
        .add(NotificationMultiSaoAreAllAddedPage(0, TransactionMode), false)
        .add(NotificationMultiSaoPreviousOfficerNamePage(1, TransactionMode), previousOfficer2Name)
        .add(NotificationMultiSaoPreviousOfficerStartDatePage(1, TransactionMode), previousOfficer2StartDate)
        .add(NotificationMultiSaoPreviousOfficerEndDatePage(1, TransactionMode), previousOfficer2EndDate)
        .add(NotificationMultiSaoAreAllAddedPage(1, TransactionMode), false)
        .add(NotificationMultiSaoPreviousOfficerNamePage(2, TransactionMode), previousOfficer3Name)
        .add(NotificationMultiSaoPreviousOfficerStartDatePage(2, TransactionMode), previousOfficer3StartDate)
        .add(NotificationMultiSaoPreviousOfficerEndDatePage(2, TransactionMode), previousOfficer3EndDate)
        .add(NotificationMultiSaoAreAllAddedPage(2, TransactionMode), true)

      val expected = UserAnswers("test")
        .add(NotificationMoreThanOneSaoPage(NormalMode), true)
        .add(NotificationMultiSaoLastOfficerNamePage(NormalMode), lastOfficerName)
        .add(NotificationMultiSaoLastOfficerStartDatePage(NormalMode), lastOfficerStartDate)
        .add(NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode), previousOfficer1Name)
        .add(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), previousOfficer1StartDate)
        .add(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), previousOfficer1EndDate)
        .add(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
        .add(NotificationMultiSaoPreviousOfficerNamePage(1, NormalMode), previousOfficer2Name)
        .add(NotificationMultiSaoPreviousOfficerStartDatePage(1, NormalMode), previousOfficer2StartDate)
        .add(NotificationMultiSaoPreviousOfficerEndDatePage(1, NormalMode), previousOfficer2EndDate)
        .add(NotificationMultiSaoAreAllAddedPage(1, NormalMode), false)
        .add(NotificationMultiSaoPreviousOfficerNamePage(2, NormalMode), previousOfficer3Name)
        .add(NotificationMultiSaoPreviousOfficerStartDatePage(2, NormalMode), previousOfficer3StartDate)
        .add(NotificationMultiSaoPreviousOfficerEndDatePage(2, NormalMode), previousOfficer3EndDate)
        .add(NotificationMultiSaoAreAllAddedPage(2, NormalMode), true)
        .add(NotificationMoreThanOneSaoPage(TransactionMode), true)
        .add(NotificationMultiSaoLastOfficerNamePage(TransactionMode), lastOfficerName)
        .add(NotificationMultiSaoLastOfficerStartDatePage(TransactionMode), lastOfficerStartDate)
        .add(NotificationMultiSaoPreviousOfficerNamePage(0, TransactionMode), previousOfficer1Name)
        .add(NotificationMultiSaoPreviousOfficerStartDatePage(0, TransactionMode), previousOfficer1StartDate)
        .add(NotificationMultiSaoPreviousOfficerEndDatePage(0, TransactionMode), previousOfficer1EndDate)
        .add(NotificationMultiSaoAreAllAddedPage(0, TransactionMode), false)
        .add(NotificationMultiSaoPreviousOfficerNamePage(1, TransactionMode), previousOfficer2Name)
        .add(NotificationMultiSaoPreviousOfficerStartDatePage(1, TransactionMode), previousOfficer2StartDate)
        .add(NotificationMultiSaoPreviousOfficerEndDatePage(1, TransactionMode), previousOfficer2EndDate)
        .add(NotificationMultiSaoAreAllAddedPage(1, TransactionMode), false)
        .add(NotificationMultiSaoPreviousOfficerNamePage(2, TransactionMode), previousOfficer3Name)
        .add(NotificationMultiSaoPreviousOfficerStartDatePage(2, TransactionMode), previousOfficer3StartDate)
        .add(NotificationMultiSaoPreviousOfficerEndDatePage(2, TransactionMode), previousOfficer3EndDate)
        .add(NotificationMultiSaoAreAllAddedPage(2, TransactionMode), true)

      val result = SUT.commitTransaction(input)
      result.data mustBe expected.data
    }
  }
}

object SaoUserAnswersServiceSpec {
  val lastOfficerName                      = "Firstname Lastname"
  val lastOfficerStartDate: LocalDate      = LocalDate.of(3, 2, 5)
  val previousOfficer1Name                 = "Firstname Lastname II"
  val previousOfficer1StartDate: LocalDate = LocalDate.of(1, 1, 1)
  val previousOfficer1EndDate: LocalDate   = LocalDate.of(2, 2, 2)
  val previousOfficer2Name                 = "Firstname Lastname III"
  val previousOfficer2StartDate: LocalDate = LocalDate.of(3, 3, 3)
  val previousOfficer2EndDate: LocalDate   = LocalDate.of(4, 4, 4)
  val previousOfficer3Name                 = "Firstname Lastname IV"
  val previousOfficer3StartDate: LocalDate = LocalDate.of(5, 3, 3)
  val previousOfficer3EndDate: LocalDate   = LocalDate.of(6, 4, 4)
  val singleOfficerName                    = "Firstname Lastname V"
  val previousOfficer4Name                 = "Firstname Lastname VI"
}
