/*
 * Copyright 2025 HM Revenue & Customs
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
import config.AppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.certificate.CertificatePendingView
import views.notification.NotificationPendingViewSpec.panelTitle

class CertificatePendingViewSpec extends ViewSpecBase[CertificatePendingView] {

  "CertificatePendingView" - {
    "must generate a view" - {
      AppConfig.setValue("senior-accounting-officer-hub-frontend.host", "hub-url")

      val doc: Document = Jsoup.parse(SUT().toString)

      doc.createTestsWithStandardPageElements(
        pageTitle = panelTitle,
        pageHeading = panelTitle,
        showBackLink = false,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      "must have a spinner" in {
        doc.select("div.loader").size() mustBe 1
      }
    }
  }
}

object CertificatePendingViewSpec {
  val panelTitle: String = "Certificate pending"
}
