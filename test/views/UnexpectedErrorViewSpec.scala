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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.UnexpectedErrorView

import UnexpectedErrorViewSpec.*

class UnexpectedErrorViewSpec extends ViewSpecBase[UnexpectedErrorView] {
  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "UnexpectedErrorView" - {
    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = false,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = true
    )

    doc.createTestsWithParagraphs(pageParagraphs)
    doc.getMainContent
      .select("p a")
      .get(0)
      .createTestWithLink(
        linkText = email,
        destinationUrl = destinationEmail
      )
    doc.createTestsWithOrWithoutError(hasError = false)
  }
}

object UnexpectedErrorViewSpec {
  val pageHeading                 = "Sorry, there is a problem with the service"
  val pageTitle                   = "Sorry, there is a problem with the service"
  val pageParagraphs: Seq[String] = Seq(
    "Try again later.",
    "Contact your Customer Compliance Manager (CCM) if you have one, or email wmbc.saomailbox@hmrc.gov.uk for support."
  )
  val email            = "wmbc.saomailbox@hmrc.gov.uk"
  val destinationEmail = "mailto:wmbc.saomailbox@hmrc.gov.uk"
}
