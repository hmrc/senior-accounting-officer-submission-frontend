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
import models.certificate.CertificateWhoIsSubmitting.*
import models.certificate.{CertificateDeclarationStandIn, CertificateWhoIsSubmitting}
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.certificate.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class CertificateCheckYourAnswersServiceSpec extends SpecBase with GuiceOneAppPerSuite with Matchers {

  private val service = new CertificateCheckYourAnswersService

  given Messages = messages(app)

  "CertificateCheckYourAnswersService" - {

    "return the expected rows when the SAO is submitting - No Addition Information" in {
      val userAnswers = userAnswersWithCertificateSaoDetails
        .set(CertificateWhoIsSubmittingPage(NormalMode), Sao)
        .success
        .value
        .set(CertificateDeclarationSaoPage(NormalMode), "Firstname Lastname")
        .success
        .value

      val result = service.getSummaryList(userAnswers)

      result.rows.length mustBe 5

      result.rows(0).key.content mustBe Text("SAO name")
      result.rows(0).value.content mustBe HtmlContent(
        """<span data-test-id="sao-full-name-value">Firstname Lastname</span>"""
      )

      result.rows(1).key.content mustBe Text("SAO email address")
      result.rows(1).value.content mustBe HtmlContent(
        """<span data-test-id="sao-email-value">firstname.lastname@example.com</span>"""
      )

      result.rows(2).key.content mustBe Text("Who is submitting the certificate?")
      result.rows(2).value.content mustBe HtmlContent(
        """<span data-test-id="who-is-submitting-value">The SAO</span>"""
      )

      result.rows(3).key.content mustBe Text("SAO name on the declaration")
      result.rows(3).value.content mustBe HtmlContent(
        """<span data-test-id="declaration-sao-value">Firstname Lastname</span>"""
      )

      result.rows(4).key.content mustBe Text("Additional information about your certificate")
      result.rows(4).value.content mustBe HtmlContent(
        """<span data-test-id="additional-information-value">Not provided</span>"""
      )
    }
    "return the expected rows when the SAO is submitting - Addition Information" in {
      val userAnswers = userAnswersWithCertificateSaoDetails
        .set(CertificateWhoIsSubmittingPage(NormalMode), Sao)
        .success
        .value
        .set(CertificateDeclarationSaoPage(NormalMode), "Firstname Lastname")
        .success
        .value
        .set(CertificateAdditionalInformationPage, Some("ABCDEFG"))
        .success
        .value

      val result = service.getSummaryList(userAnswers)

      result.rows.length mustBe 5

      result.rows(0).key.content mustBe Text("SAO name")
      result.rows(0).value.content mustBe HtmlContent(
        """<span data-test-id="sao-full-name-value">Firstname Lastname</span>"""
      )

      result.rows(1).key.content mustBe Text("SAO email address")
      result.rows(1).value.content mustBe HtmlContent(
        """<span data-test-id="sao-email-value">firstname.lastname@example.com</span>"""
      )

      result.rows(2).key.content mustBe Text("Who is submitting the certificate?")
      result.rows(2).value.content mustBe HtmlContent(
        """<span data-test-id="who-is-submitting-value">The SAO</span>"""
      )

      result.rows(3).key.content mustBe Text("SAO name on the declaration")
      result.rows(3).value.content mustBe HtmlContent(
        """<span data-test-id="declaration-sao-value">Firstname Lastname</span>"""
      )

      result.rows(4).key.content mustBe Text("Additional information about your certificate")
      result.rows(4).value.content mustBe HtmlContent(
        """<span data-test-id="additional-information-value">ABCDEFG</span>"""
      )
    }

    "return the expected rows when a stand-in is submitting - No Addition Information" in {
      val userAnswers = userAnswersWithCertificateSaoDetails
        .set(CertificateWhoIsSubmittingPage(NormalMode), StandIn)
        .success
        .value
        .set(
          CertificateDeclarationStandInPage(NormalMode),
          CertificateDeclarationStandIn("Stand-in Name", "SAO Name")
        )
        .success
        .value

      val result = service.getSummaryList(userAnswers)

      result.rows.length mustBe 6

      result.rows(0).key.content mustBe Text("SAO name")
      result.rows(0).value.content mustBe HtmlContent(
        """<span data-test-id="sao-full-name-value">Firstname Lastname</span>"""
      )

      result.rows(1).key.content mustBe Text("SAO email address")
      result.rows(1).value.content mustBe HtmlContent(
        """<span data-test-id="sao-email-value">firstname.lastname@example.com</span>"""
      )

      result.rows(2).key.content mustBe Text("Who is submitting the certificate?")
      result.rows(2).value.content mustBe HtmlContent(
        """<span data-test-id="who-is-submitting-value">A person authorised to submit on behalf of the SAO</span>"""
      )

      result.rows(3).key.content mustBe Text("Authorised name on the declaration")
      result.rows(3).value.content mustBe HtmlContent(
        """<span data-test-id="declaration-stand-in-value">Stand-in Name</span>"""
      )

      result.rows(4).key.content mustBe Text("SAO name on the declaration")
      result.rows(4).value.content mustBe HtmlContent(
        """<span data-test-id="declaration-sao-value">SAO Name</span>"""
      )

      result.rows(5).key.content mustBe Text("Additional information about your certificate")
      result.rows(5).value.content mustBe HtmlContent(
        """<span data-test-id="additional-information-value">Not provided</span>"""
      )
    }

    "return the expected rows when a stand-in is submitting - Addition Information" in {
      val userAnswers = userAnswersWithCertificateSaoDetails
        .set(CertificateWhoIsSubmittingPage(NormalMode), StandIn)
        .success
        .value
        .set(
          CertificateDeclarationStandInPage(NormalMode),
          CertificateDeclarationStandIn("Stand-in Name", "SAO Name")
        )
        .success
        .value
        .set(CertificateAdditionalInformationPage, Some("ABCDEFG"))
        .success
        .value

      val result = service.getSummaryList(userAnswers)

      result.rows.length mustBe 6

      result.rows(0).key.content mustBe Text("SAO name")
      result.rows(0).value.content mustBe HtmlContent(
        """<span data-test-id="sao-full-name-value">Firstname Lastname</span>"""
      )

      result.rows(1).key.content mustBe Text("SAO email address")
      result.rows(1).value.content mustBe HtmlContent(
        """<span data-test-id="sao-email-value">firstname.lastname@example.com</span>"""
      )

      result.rows(2).key.content mustBe Text("Who is submitting the certificate?")
      result.rows(2).value.content mustBe HtmlContent(
        """<span data-test-id="who-is-submitting-value">A person authorised to submit on behalf of the SAO</span>"""
      )

      result.rows(3).key.content mustBe Text("Authorised name on the declaration")
      result.rows(3).value.content mustBe HtmlContent(
        """<span data-test-id="declaration-stand-in-value">Stand-in Name</span>"""
      )

      result.rows(4).key.content mustBe Text("SAO name on the declaration")
      result.rows(4).value.content mustBe HtmlContent(
        """<span data-test-id="declaration-sao-value">SAO Name</span>"""
      )

      result.rows(5).key.content mustBe Text("Additional information about your certificate")
      result.rows(5).value.content mustBe HtmlContent(
        """<span data-test-id="additional-information-value">ABCDEFG</span>"""
      )
    }
  }
}
