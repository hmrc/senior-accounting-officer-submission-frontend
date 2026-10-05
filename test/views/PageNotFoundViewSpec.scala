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
import views.html.PageNotFoundView

import PageNotFoundViewSpec.*

class PageNotFoundViewSpec extends ViewSpecBase[PageNotFoundView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "PageNotFoundView" - {

    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = false,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithParagraphs(
      pageParagraphs
    )

    doc.getMainContent
      .select("p a")
      .first()
      .createTestWithLink(
        linkText = supportEmailLinkText,
        destinationUrl = supportEmailMailto
      )

    doc.createTestsWithOrWithoutError(hasError = false)
  }
}

object PageNotFoundViewSpec {
  val pageHeading = "Page not found"
  val pageTitle   = "Error: Page not found"

  val supportEmailLinkText = "wmbc.saomailbox@hmrc.gov.uk"
  val supportEmailMailto   = "mailto:wmbc.saomailbox@hmrc.gov.uk"

  val pageParagraphs: Seq[String] = Seq(
    "If you typed the web address, check it is correct.",
    "If you pasted the web address, check you copied the entire address.",
    "If the web address is correct or you selected a link or button, contact your Customer Compliance Manager (CCM) if you have one, or email wmbc.saomailbox@hmrc.gov.uk for support."
  )
}
