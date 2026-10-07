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

package views

import base.ViewSpecBase
import config.AppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.JourneyRecoveryStartAgainView

import JourneyRecoveryStartAgainViewSpec.*

class JourneyRecoveryStartAgainViewSpec extends ViewSpecBase[JourneyRecoveryStartAgainView] {

  private def generateView(section: Option[String]): Document = Jsoup.parse(SUT(section).toString)

  private def createTestWithStartAgainButton(doc: Document): Unit =
    "must have a start again button linking to the hub" in {
      val link = doc.getMainContent.select("a.govuk-button").first()
      link.text() mustBe buttonText
      link.attr("href") mustBe hubUrl
    }

  "JourneyRecoveryStartAgainView" - {

    "when no section is provided" - {
      AppConfig.setValue("hub-frontend.host", hubHost)
      val doc: Document = generateView(section = None)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitleWithoutSection,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithParagraphs(pageParagraphs)

      createTestWithStartAgainButton(doc)
    }

    "when the notification section is provided" - {
      AppConfig.setValue("hub-frontend.host", hubHost)
      val doc: Document = generateView(section = Some("journeyRecovery.section.notification"))

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitleWithNotificationSection,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithParagraphs(pageParagraphs)

      createTestWithStartAgainButton(doc)
    }

    "when the certificate section is provided" - {
      AppConfig.setValue("hub-frontend.host", hubHost)
      val doc: Document = generateView(section = Some("journeyRecovery.section.certificate"))

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitleWithCertificateSection,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithParagraphs(pageParagraphs)

      createTestWithStartAgainButton(doc)
    }
  }
}

object JourneyRecoveryStartAgainViewSpec {
  val pageHeading = "Sorry, there is a problem with the service"

  val pageParagraphs: Seq[String] = Seq(
    "We were unable to access your data due to a technical problem.",
    "Start again"
  )

  val pageTitleWithoutSection: String =
    "Error: Sorry, there is a problem with the service"

  val pageTitleWithNotificationSection: String =
    "Error: Sorry, there is a problem with the service - Notification"

  val pageTitleWithCertificateSection: String =
    "Error: Sorry, there is a problem with the service - Certificate"

  val buttonText     = "Start again"
  val hubHost        = "testHubUrl"
  val hubUrl: String = s"$hubHost/senior-accounting-officer"
}
