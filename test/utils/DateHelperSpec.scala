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

package utils

import base.SpecBase

import java.time.*
import java.time.temporal.ChronoUnit

class DateHelperSpec extends SpecBase {

  val UTC         = ZoneOffset.UTC
  val BST: ZoneId = ZoneId.of("UTC-1")

  def SUT(instant: Instant): DateHelper =
    // using ZoneOffset.UTC here since Clock is set to UTC in modules
    DateHelper(Clock.fixed(instant, ZoneOffset.UTC))

  "DateHelper.nowUkLocalDate when" - {
    "UK is in GMT must" - {
      "return the same LocalDate as UTC when the system clock is at 00:00:00 UTC and the local date" in {
        val testInstant = ZonedDateTime.of(2020, 1, 27, 0, 0, 0, 0, UTC).toInstant.truncatedTo(ChronoUnit.MILLIS)

        val dateHelper = SUT(testInstant)

        val date = LocalDate.of(2020, 1, 27)

        dateHelper.nowUkLocalDate mustBe date
      }
    }
    "UK is in BST must" - {
      "return the same LocalDate as UTC when the system clock is at 22:59:59 UTC" in {
        val testInstant = ZonedDateTime.of(2020, 9, 27, 0, 22, 59, 59, UTC).toInstant.truncatedTo(ChronoUnit.MILLIS)

        val dateHelper = SUT(testInstant)

        val date = LocalDate.of(2020, 9, 27)

        dateHelper.nowUkLocalDate mustBe date
      }
      "return the LocalDate for the day after the UTC's LocalDate when the system clock is at 23:00:00 UTC" in {
        val testInstant = ZonedDateTime.of(2020, 9, 27, 23, 0, 0, 0, UTC).toInstant.truncatedTo(ChronoUnit.MILLIS)

        val dateHelper = SUT(testInstant)

        val date = LocalDate.of(2020, 9, 28)

        dateHelper.nowUkLocalDate mustBe date
      }
    }
  }

  "FutureDateHelper.isFutureDate when" - {
    "UK is in GMT must" - {
      "return false when the system clock is at 00:00:00 UTC and the local date is the previous day to the system date" in {
        val testInstant = ZonedDateTime.of(2020, 1, 27, 0, 0, 0, 0, UTC).toInstant.truncatedTo(ChronoUnit.MILLIS)

        val dateHelper       = SUT(testInstant)
        val futureDateHelper = dateHelper.futureDateHelper

        import futureDateHelper.*

        val date = LocalDate.of(2020, 1, 26)

        date.isFutureDate mustBe false
      }
      "return true when the system clock is at 00:00:00 UTC and the local date is the same day as the system date" in {
        val testInstant = ZonedDateTime.of(2020, 1, 27, 0, 0, 0, 0, UTC).toInstant.truncatedTo(ChronoUnit.MILLIS)

        val dateHelper       = SUT(testInstant)
        val futureDateHelper = dateHelper.futureDateHelper

        import futureDateHelper.*

        val date = LocalDate.of(2020, 1, 27)

        date.isFutureDate mustBe true
      }
      "return true when the system clock is at 00:00:00 UTC and the local date after the system date" in {
        val testInstant = ZonedDateTime.of(2020, 1, 27, 0, 0, 0, 0, UTC).toInstant.truncatedTo(ChronoUnit.MILLIS)

        val dateHelper       = SUT(testInstant)
        val futureDateHelper = dateHelper.futureDateHelper

        import futureDateHelper.*

        val date = LocalDate.of(2020, 1, 28)

        date.isFutureDate mustBe true
      }
    }

    "UK is in BST must" - {
      "return false when the system clock is at 00:00:00 UTC and the local date is the previous day to the system date" in {
        val testInstant = ZonedDateTime.of(2020, 9, 27, 0, 0, 0, 0, UTC).toInstant.truncatedTo(ChronoUnit.MILLIS)

        val dateHelper       = SUT(testInstant)
        val futureDateHelper = dateHelper.futureDateHelper

        import futureDateHelper.*

        val date = LocalDate.of(2020, 9, 26)

        date.isFutureDate mustBe false
        SUT(testInstant).nowUkLocalDate mustBe LocalDate.of(2020, 9, 27)
      }
      "return false when the system clock is at 23:00:00 UTC and the local date is the same day as the system date" in {
        val testInstant = ZonedDateTime.of(2020, 9, 27, 23, 0, 0, 0, UTC).toInstant.truncatedTo(ChronoUnit.MILLIS)

        val dateHelper       = SUT(testInstant)
        val futureDateHelper = dateHelper.futureDateHelper

        import futureDateHelper.*

        val date = LocalDate.of(2020, 9, 27)

        date.isFutureDate mustBe false
        dateHelper.nowUkLocalDate mustBe LocalDate.of(2020, 9, 28)
      }
      "return true when the system clock is at 22:59:59 UTC and the local date is the same day as the system date" in {
        val testInstant = ZonedDateTime.of(2020, 9, 27, 22, 59, 59, 0, UTC).toInstant.truncatedTo(ChronoUnit.MILLIS)

        val dateHelper       = SUT(testInstant)
        val futureDateHelper = dateHelper.futureDateHelper

        import futureDateHelper.*

        val date = LocalDate.of(2020, 9, 27)

        date.isFutureDate mustBe true
        dateHelper.nowUkLocalDate mustBe LocalDate.of(2020, 9, 27)
      }
      "return true when the system clock is at 00:00:00 UTC and the local date after the system date" in {
        val testInstant = ZonedDateTime.of(2020, 9, 27, 0, 0, 0, 0, UTC).toInstant.truncatedTo(ChronoUnit.MILLIS)

        val dateHelper       = SUT(testInstant)
        val futureDateHelper = dateHelper.futureDateHelper

        import futureDateHelper.*

        val date = LocalDate.of(2020, 9, 28)

        date.isFutureDate mustBe true
        dateHelper.nowUkLocalDate mustBe LocalDate.of(2020, 9, 27)
      }
    }

  }

  ".isFutureDate must return true when" - {
    "UK is in GMT and local date is in the same date as the Date at 00:00:00 UTC+0:00" in {
      val testInstant = ZonedDateTime.of(2020, 1, 27, 0, 0, 0, 0, UTC).toInstant.truncatedTo(ChronoUnit.MILLIS)

      val dateHelper       = SUT(testInstant)
      val futureDateHelper = dateHelper.futureDateHelper

      import futureDateHelper.*

      val date = LocalDate.of(2020, 1, 27)

      date.isFutureDate mustBe true
    }
    "UK is in GMT and local date is a day after the date as the Date at 00:00:00 UTC+0:00" in {
      val testInstant = ZonedDateTime.of(2020, 1, 27, 0, 0, 0, 0, BST).toInstant.truncatedTo(ChronoUnit.MILLIS)

      val dateHelper       = SUT(testInstant)
      val futureDateHelper = dateHelper.futureDateHelper

      import futureDateHelper.*

      val date = LocalDate.of(2020, 1, 27)

      date.isFutureDate mustBe true
    }
    "UK is in BST and local date is in the same date as the Date at 00:00:00 UTC+1:00 " in {
      val testInstant = ZonedDateTime.of(2020, 9, 27, 0, 0, 0, 0, UTC).toInstant.truncatedTo(ChronoUnit.MILLIS)

      val dateHelper       = SUT(testInstant)
      val futureDateHelper = dateHelper.futureDateHelper

      import futureDateHelper.*

      val date = LocalDate.of(2020, 9, 27)

      date.isFutureDate mustBe true
    }
    "UK is in BST and local date is a day after the date as the Date at 00:00:00 UTC+1:00 " in {
      val testInstant = ZonedDateTime.of(2020, 9, 27, 0, 0, 0, 0, BST).toInstant.truncatedTo(ChronoUnit.MILLIS)

      val dateHelper       = SUT(testInstant)
      val futureDateHelper = dateHelper.futureDateHelper

      import futureDateHelper.*

      val date = LocalDate.of(2020, 9, 28)

      date.isFutureDate mustBe true
    }
  }

  ".isFutureDate must return false when" - {
    "UK is in GMT and local date is in the previous day as the Date at 00:00:00 UTC+0:00" in {
      val testInstant = ZonedDateTime.of(2020, 1, 27, 0, 0, 0, 0, UTC).toInstant.truncatedTo(ChronoUnit.MILLIS)

      val dateHelper = SUT(testInstant).futureDateHelper

      import dateHelper.*

      val date = LocalDate.of(2020, 1, 26)

      date.isFutureDate mustBe false
    }
    "UK is in BST and local date is in the previous day as the Date at 00:00:00 UTC+1:00" in {
      val testInstant = ZonedDateTime.of(2020, 9, 27, 0, 0, 0, 0, BST).toInstant.truncatedTo(ChronoUnit.MILLIS)

      val dateHelper = SUT(testInstant).futureDateHelper

      import dateHelper.*

      val date = LocalDate.of(2020, 9, 26)

      date.isFutureDate mustBe false
    }
    "UK is in BST and local date is in the same day as the Date at 23:00:00 UTC+0:00" in {
      val testInstant =
        ZonedDateTime.of(2020, 9, 27, 23, 0, 0, 0, ZoneOffset.UTC).toInstant.truncatedTo(ChronoUnit.MILLIS)

      val dateHelper = SUT(testInstant).futureDateHelper

      import dateHelper.*

      val date = LocalDate.of(2020, 9, 27)

      date.isFutureDate mustBe false
    }
  }

}
