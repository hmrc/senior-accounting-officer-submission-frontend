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

package views.certificate

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.certificate.CertificateUploadSuccessViewSpec.*
import views.html.certificate.CertificateUploadSuccessView

class CertificateUploadSuccessViewSpec extends ViewSpecBase[CertificateUploadSuccessView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "CertificateUploadSuccessView" - {
    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = true,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithOrWithoutError(hasError = false)

    doc.createTestsWithParagraphs(paragraphs)

    "must have a spinner" in {
      doc.select("div.loader").size() mustBe 1
    }

    "must refresh the page without adding a browser history entry" in {
      doc.select("script").html() must include("window.location.replace(window.location.pathname)")
      doc.select("script").html() must not include "window.location.href"
    }
  }
}

object CertificateUploadSuccessViewSpec {
  val pageHeading = "Your submission template is uploading"
  val pageTitle   = "Upload a submission template for your certificate"

  val paragraphs: List[String] = List("This may take a few minutes")
}
