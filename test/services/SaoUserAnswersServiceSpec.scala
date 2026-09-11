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
import models.UserAnswers
import pages.notification.*

import java.time.LocalDate

import SaoUserAnswersServiceSpec.*
import models.NormalMode

class SaoUserAnswersServiceSpec extends SpecBase {

  def SUT = new SaoUserAnswersService

  "cleanupMultiSaoDataAfterIndex" - {
    "User answered yes to are all saos added at provided sao index" - {
      "Subsequent multi sao data is removed from user answers" in {

        val input = UserAnswers("test")
          .set(NotificationMoreThanOneSaoPage(NormalMode), true)
          .get
          .set(NotificationMultiSaoLastOfficerNamePage(NormalMode), lastOfficerName)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode), previousOfficer1Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), previousOfficer1StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), previousOfficer1EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(1, NormalMode), previousOfficer2Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(1, NormalMode), previousOfficer2StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(1, NormalMode), previousOfficer2EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(1, NormalMode), true)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(2, NormalMode), previousOfficer3Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(2, NormalMode), previousOfficer3StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(2, NormalMode), previousOfficer3EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(2, NormalMode), true)
          .get

        val expected = UserAnswers("test")
          .set(NotificationMoreThanOneSaoPage(NormalMode), true)
          .get
          .set(NotificationMultiSaoLastOfficerNamePage(NormalMode), lastOfficerName)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode), previousOfficer1Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), previousOfficer1StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), previousOfficer1EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(1, NormalMode), previousOfficer2Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(1, NormalMode), previousOfficer2StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(1, NormalMode), previousOfficer2EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(1, NormalMode), true)
          .get

        val result = SUT.cleanupMultiSaoDataAfterIndex(input, 1)
        result.data mustBe expected.data
      }
    }

    "User answered no to are all saos added at provided sao index" - {
      "No change is made to user answers" in {

        val input = UserAnswers("test")
          .set(NotificationMoreThanOneSaoPage(NormalMode), true)
          .get
          .set(NotificationMultiSaoLastOfficerNamePage(NormalMode), lastOfficerName)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode), previousOfficer1Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), previousOfficer1StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), previousOfficer1EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(1, NormalMode), previousOfficer2Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(1, NormalMode), previousOfficer2StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(1, NormalMode), previousOfficer2EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(1, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(2, NormalMode), previousOfficer3Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(2, NormalMode), previousOfficer3StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(2, NormalMode), previousOfficer3EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(2, NormalMode), true)
          .get

        val expected = input
        val result   = SUT.cleanupMultiSaoDataAfterIndex(input, 0)
        result.data mustBe expected.data
      }
    }
  }

  "sanitiseUserAnswers" - {
    "user has provided details for a single sao" - {
      "multi sao user answers are pruned" in {
        val input = UserAnswers("test")
          .set(NotificationMoreThanOneSaoPage(NormalMode), false)
          .get
          .set(NotificationSingleSaoOfficerNamePage(NormalMode), singleOfficerName)
          .get
          .set(NotificationMultiSaoLastOfficerNamePage(NormalMode), lastOfficerName)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode), previousOfficer1Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), previousOfficer1StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), previousOfficer1EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(1, NormalMode), previousOfficer2Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(1, NormalMode), previousOfficer2StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(1, NormalMode), previousOfficer2EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(1, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(2, NormalMode), previousOfficer3Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(2, NormalMode), previousOfficer3StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(2, NormalMode), previousOfficer3EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(2, NormalMode), true)
          .get

        val expected = UserAnswers("test")
          .set(NotificationMoreThanOneSaoPage(NormalMode), false)
          .get
          .set(NotificationSingleSaoOfficerNamePage(NormalMode), singleOfficerName)
          .get

        val result = SUT.sanitiseUserAnswers(input)
        result.data mustBe expected.data
      }
    }

    "user has provided details for multiple saos" - {
      "single sao user answers are pruned, last complete sao is marked as the final sao and incomplete sao data is removed" in {
        val input = UserAnswers("test")
          .set(NotificationMoreThanOneSaoPage(NormalMode), true)
          .get
          .set(NotificationSingleSaoOfficerNamePage(NormalMode), singleOfficerName)
          .get
          .set(NotificationMultiSaoLastOfficerNamePage(NormalMode), lastOfficerName)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode), previousOfficer1Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), previousOfficer1StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), previousOfficer1EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(1, NormalMode), previousOfficer2Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(1, NormalMode), previousOfficer2StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(1, NormalMode), previousOfficer2EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(1, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(2, NormalMode), previousOfficer3Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(2, NormalMode), previousOfficer3StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(2, NormalMode), previousOfficer3EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(2, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(3, NormalMode), previousOfficer4Name)
          .get

        val expected = UserAnswers("test")
          .set(NotificationMoreThanOneSaoPage(NormalMode), true)
          .get
          .set(NotificationMultiSaoLastOfficerNamePage(NormalMode), lastOfficerName)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode), previousOfficer1Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), previousOfficer1StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), previousOfficer1EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(1, NormalMode), previousOfficer2Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(1, NormalMode), previousOfficer2StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(1, NormalMode), previousOfficer2EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(1, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(2, NormalMode), previousOfficer3Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(2, NormalMode), previousOfficer3StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(2, NormalMode), previousOfficer3EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(2, NormalMode), true)
          .get

        val result = SUT.sanitiseUserAnswers(input)
        result.data mustBe expected.data
      }
    }
  }

  "removeOtherSaoJourneyData" - {
    "user has provided details for a single sao" - {
      "multi sao user answers are pruned" in {
        val input = UserAnswers("test")
          .set(NotificationMoreThanOneSaoPage(NormalMode), false)
          .get
          .set(NotificationSingleSaoOfficerNamePage(NormalMode), singleOfficerName)
          .get
          .set(NotificationMultiSaoLastOfficerNamePage(NormalMode), lastOfficerName)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode), previousOfficer1Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), previousOfficer1StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), previousOfficer1EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(1, NormalMode), previousOfficer2Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(1, NormalMode), previousOfficer2StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(1, NormalMode), previousOfficer2EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(1, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(2, NormalMode), previousOfficer3Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(2, NormalMode), previousOfficer3StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(2, NormalMode), previousOfficer3EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(2, NormalMode), true)
          .get

        val expected = UserAnswers("test")
          .set(NotificationMoreThanOneSaoPage(NormalMode), false)
          .get
          .set(NotificationSingleSaoOfficerNamePage(NormalMode), singleOfficerName)
          .get

        val result = SUT.removeOtherSaoJourneyData(input)
        result.data mustBe expected.data
      }
    }

    "user has provided details for multiple saos" - {
      "single sao user answers are pruned" in {
        val input = UserAnswers("test")
          .set(NotificationMoreThanOneSaoPage(NormalMode), true)
          .get
          .set(NotificationSingleSaoOfficerNamePage(NormalMode), singleOfficerName)
          .get
          .set(NotificationMultiSaoLastOfficerNamePage(NormalMode), lastOfficerName)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode), previousOfficer1Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), previousOfficer1StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), previousOfficer1EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(1, NormalMode), previousOfficer2Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(1, NormalMode), previousOfficer2StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(1, NormalMode), previousOfficer2EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(1, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(2, NormalMode), previousOfficer3Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(2, NormalMode), previousOfficer3StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(2, NormalMode), previousOfficer3EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(2, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(3, NormalMode), previousOfficer4Name)
          .get

        val expected = UserAnswers("test")
          .set(NotificationMoreThanOneSaoPage(NormalMode), true)
          .get
          .set(NotificationMultiSaoLastOfficerNamePage(NormalMode), lastOfficerName)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode), previousOfficer1Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode), previousOfficer1StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode), previousOfficer1EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(0, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(1, NormalMode), previousOfficer2Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(1, NormalMode), previousOfficer2StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(1, NormalMode), previousOfficer2EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(1, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(2, NormalMode), previousOfficer3Name)
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(2, NormalMode), previousOfficer3StartDate)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(2, NormalMode), previousOfficer3EndDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(2, NormalMode), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(3, NormalMode), previousOfficer4Name)
          .get

        val result = SUT.removeOtherSaoJourneyData(input)
        result.data mustBe expected.data
      }
    }
  }
}

object SaoUserAnswersServiceSpec {
  val lastOfficerName                      = "Firstname Lastname"
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
