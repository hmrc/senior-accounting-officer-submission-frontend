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
import models.*
import models.certificate.CertificateWhoIsSubmitting
import pages.certificate.*
import models.certificate.CertificateDeclarationStandIn

class DeclarationUserAnswersServiceSpec extends SpecBase {

  def SUT = new DeclarationUserAnswersService

  "sanitise" - {
    "the user provides an sao declaration" in {
      val input = emptyUserAnswers
        .add(CertificateWhoIsSubmittingPage(NormalMode), CertificateWhoIsSubmitting.Sao)
        .add(CertificateDeclarationStandInPage(NormalMode), CertificateDeclarationStandIn("a", "b"))
        .add(CertificateDeclarationSaoPage(NormalMode), "c")
        .add(CertificateWhoIsSubmittingPage(TransactionMode), CertificateWhoIsSubmitting.StandIn)
        .add(CertificateDeclarationStandInPage(TransactionMode), CertificateDeclarationStandIn("y", "x"))
        .add(CertificateDeclarationSaoPage(TransactionMode), "z")

      val expected = emptyUserAnswers
        .add(CertificateWhoIsSubmittingPage(NormalMode), CertificateWhoIsSubmitting.Sao)
        .add(CertificateDeclarationSaoPage(NormalMode), "c")
        .add(CertificateWhoIsSubmittingPage(TransactionMode), CertificateWhoIsSubmitting.Sao)
        .add(CertificateDeclarationSaoPage(TransactionMode), "c")

      expected.data mustBe SUT.sanitise(input).data
    }

    "the user provides a stand in declaration" in {
      val input = emptyUserAnswers
        .add(CertificateWhoIsSubmittingPage(NormalMode), CertificateWhoIsSubmitting.StandIn)
        .add(CertificateDeclarationStandInPage(NormalMode), CertificateDeclarationStandIn("a", "b"))
        .add(CertificateDeclarationSaoPage(NormalMode), "c")
        .add(CertificateWhoIsSubmittingPage(TransactionMode), CertificateWhoIsSubmitting.Sao)
        .add(CertificateDeclarationStandInPage(TransactionMode), CertificateDeclarationStandIn("y", "x"))
        .add(CertificateDeclarationSaoPage(TransactionMode), "z")

      val expected = emptyUserAnswers
        .add(CertificateWhoIsSubmittingPage(NormalMode), CertificateWhoIsSubmitting.StandIn)
        .add(CertificateDeclarationStandInPage(NormalMode), CertificateDeclarationStandIn("a", "b"))
        .add(CertificateWhoIsSubmittingPage(TransactionMode), CertificateWhoIsSubmitting.StandIn)
        .add(CertificateDeclarationStandInPage(TransactionMode), CertificateDeclarationStandIn("a", "b"))

      expected.data mustBe SUT.sanitise(input).data
    }

    "the type of declaration is not specified" in {
      val input = emptyUserAnswers
        .add(CertificateDeclarationStandInPage(NormalMode), CertificateDeclarationStandIn("a", "b"))
        .add(CertificateDeclarationSaoPage(NormalMode), "c")
        .add(CertificateWhoIsSubmittingPage(TransactionMode), CertificateWhoIsSubmitting.Sao)
        .add(CertificateDeclarationStandInPage(TransactionMode), CertificateDeclarationStandIn("y", "x"))
        .add(CertificateDeclarationSaoPage(TransactionMode), "z")

      intercept[NotImplementedError] {
        SUT.sanitise(input)
      }
    }
  }

  "commitTransaction" - {
    "the user provides an sao declaration" in {
      val input =
        emptyUserAnswers
          .add(CertificateWhoIsSubmittingPage(TransactionMode), CertificateWhoIsSubmitting.Sao)
          .add(CertificateDeclarationSaoPage(TransactionMode), "a")
          .add(CertificateWhoIsSubmittingPage(NormalMode), CertificateWhoIsSubmitting.StandIn)
          .add(CertificateDeclarationSaoPage(NormalMode), "will be overwritten")

      val expected = emptyUserAnswers
        .add(CertificateWhoIsSubmittingPage(TransactionMode), CertificateWhoIsSubmitting.Sao)
        .add(CertificateDeclarationSaoPage(TransactionMode), "a")
        .add(CertificateWhoIsSubmittingPage(NormalMode), CertificateWhoIsSubmitting.Sao)
        .add(CertificateDeclarationSaoPage(NormalMode), "a")

      expected.data mustBe SUT.commitTransaction(input).data
    }

    "the user provides a stand in declaration" in {
      val input = emptyUserAnswers
        .add(CertificateWhoIsSubmittingPage(TransactionMode), CertificateWhoIsSubmitting.StandIn)
        .add(CertificateDeclarationStandInPage(TransactionMode), CertificateDeclarationStandIn("b", "c"))
        .add(CertificateWhoIsSubmittingPage(NormalMode), CertificateWhoIsSubmitting.Sao)
        .add(CertificateDeclarationStandInPage(NormalMode), CertificateDeclarationStandIn("will be", "overwritten"))

      val expected = emptyUserAnswers
        .add(CertificateWhoIsSubmittingPage(TransactionMode), CertificateWhoIsSubmitting.StandIn)
        .add(CertificateDeclarationStandInPage(TransactionMode), CertificateDeclarationStandIn("b", "c"))
        .add(CertificateWhoIsSubmittingPage(NormalMode), CertificateWhoIsSubmitting.StandIn)
        .add(CertificateDeclarationStandInPage(NormalMode), CertificateDeclarationStandIn("b", "c"))

      expected.data mustBe SUT.commitTransaction(input).data
    }

    "no answer is provided" in {
      val input = emptyUserAnswers
        .add(CertificateDeclarationStandInPage(TransactionMode), CertificateDeclarationStandIn("b", "c"))
        .add(CertificateWhoIsSubmittingPage(NormalMode), CertificateWhoIsSubmitting.Sao)
        .add(CertificateDeclarationStandInPage(NormalMode), CertificateDeclarationStandIn("will be", "overwritten"))

      intercept[NotImplementedError] {
        SUT.commitTransaction(input)
      }
    }
  }
}
