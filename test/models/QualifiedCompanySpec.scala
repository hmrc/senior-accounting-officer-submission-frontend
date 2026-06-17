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

package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import models.displayRegimes
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import models.QualifiedCompanySpec.*

class QualifiedCompanySpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite {
  given Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  "displayRegimes" - {
    "when there are no active regimes on a qualified company must return an empty string" in {
      val sut = QualifiedCompany(
        name = "",
        utr = "",
        corporationTax = false,
        valueAddedTax = false,
        paye = false,
        insurancePremiumTax = false,
        stampDutyLandTax = false,
        stampDutyReserveTax = false,
        petroleumRevenueTax = false,
        customsDuties = false,
        exciseDuties = false,
        bankLevy = false,
        additionalInformation = ""
      )

      sut.displayRegimes mustEqual ""
    }

    "when corporation tax is the only active regime on a qualified company must return the expected string" in {
      val sut = QualifiedCompany(
        name = "",
        utr = "",
        corporationTax = true,
        valueAddedTax = false,
        paye = false,
        insurancePremiumTax = false,
        stampDutyLandTax = false,
        stampDutyReserveTax = false,
        petroleumRevenueTax = false,
        customsDuties = false,
        exciseDuties = false,
        bankLevy = false,
        additionalInformation = ""
      )

      sut.displayRegimes mustEqual corporationTaxText
    }

    "when corporation tax and value added tax are active regimes on a qualified company must return the expected string" in {
      val sut = QualifiedCompany(
        name = "",
        utr = "",
        corporationTax = true,
        valueAddedTax = true,
        paye = false,
        insurancePremiumTax = false,
        stampDutyLandTax = false,
        stampDutyReserveTax = false,
        petroleumRevenueTax = false,
        customsDuties = false,
        exciseDuties = false,
        bankLevy = false,
        additionalInformation = ""
      )

      sut.displayRegimes mustEqual s"$corporationTaxText, $valueAddedTaxText"
    }

    "when all regimes are active on a qualified company must return the expected string" in {
      val sut = QualifiedCompany(
        name = "",
        utr = "",
        corporationTax = true,
        valueAddedTax = true,
        paye = true,
        insurancePremiumTax = true,
        stampDutyLandTax = true,
        stampDutyReserveTax = true,
        petroleumRevenueTax = true,
        customsDuties = true,
        exciseDuties = true,
        bankLevy = true,
        additionalInformation = ""
      )

      sut.displayRegimes mustEqual s"$corporationTaxText, $valueAddedTaxText, $payeText, $insurancePremiumTaxText, $stampDutyLandTaxText, $stampDutyReserveTaxText, $petroleumRevenueTaxText, $customsDutiesText, $exciseDutiesText, $bankLevyText"
    }
  }
}

object QualifiedCompanySpec {
  val corporationTaxText      = "Corporation Tax"
  val valueAddedTaxText       = "Value Added Tax"
  val payeText                = "PAYE (Pay As You Earn)"
  val insurancePremiumTaxText = "Insurance Premium Tax"
  val stampDutyLandTaxText    = "Stamp Duty Land Tax"
  val stampDutyReserveTaxText = "Stamp Duty Reserve Tax"
  val petroleumRevenueTaxText = "Petroleum Revenue Tax"
  val customsDutiesText       = "Customs Duties"
  val exciseDutiesText        = "Excise Duties"
  val bankLevyText            = "Bank Levy"
}
