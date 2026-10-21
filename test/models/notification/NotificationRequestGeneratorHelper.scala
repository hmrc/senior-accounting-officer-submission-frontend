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

package models.notification

import generators.Generators.stringsWithoutInvalidSymbolsWithMaxLength
import models.upload.{CompanyStatus, CompanyType}
import org.scalacheck.Gen

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object NotificationRequestGeneratorHelper {
  private val dateFormatter              = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  private val maxLength                  = 105
  private val currentYear                = LocalDate.now().getYear
  private val dateStringGen: Gen[String] =
    for {
      year  <- Gen.choose(1900, currentYear)
      month <- Gen.choose(1, 12)
      day   <- Gen.choose(
        1,
        LocalDate.of(year, month, 1).lengthOfMonth()
      )
    } yield LocalDate.of(year, month, day).format(dateFormatter)

  val companyGen: Gen[Company] =
    for {
      crn          <- Gen.option(Gen.listOfN(10, Gen.numChar).map(_.mkString))
      utr          <- Gen.listOfN(10, Gen.numChar).map(_.mkString)
      name         <- Gen.nonEmptyListOf(stringsWithoutInvalidSymbolsWithMaxLength(maxLength)).map(_.mkString)
      accPeriodEnd <- Gen.nonEmptyListOf(dateStringGen).map(_.mkString)
      status       <- Gen.oneOf(CompanyStatus.values.toSeq)
      companyType  <- Gen.oneOf(CompanyType.values.toSeq)
    } yield Company(crn, utr, name, accPeriodEnd, status, companyType)

  val saoGen: Gen[Sao] =
    for {
      name     <- Gen.nonEmptyListOf(stringsWithoutInvalidSymbolsWithMaxLength(maxLength)).map(_.mkString)
      fromDate <- Gen.option(dateStringGen)
      toDate   <- Gen.option(dateStringGen)
    } yield Sao(name, fromDate, toDate)

  val notificationRequestGen: Gen[NotificationRequest] =
    for {
      companies      <- Gen.nonEmptyListOf(companyGen)
      saos           <- Gen.nonEmptyListOf(saoGen)
      remarks        <- Gen.option(stringsWithoutInvalidSymbolsWithMaxLength(maxLength))
      idempotencyKey <- Gen.option(Gen.alphaNumStr.map(_.take(100)))
    } yield NotificationRequest(companies, saos, remarks, idempotencyKey)
}
