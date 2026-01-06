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

package views

import base.ViewSpecBase
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.CertificateCheckYourAnswersViewSpec.*
import views.html.CertificateCheckYourAnswersView

class CertificateCheckYourAnswersViewSpec extends ViewSpecBase[CertificateCheckYourAnswersView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "CertificateCheckYourAnswersView" - {
    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = true,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithOrWithoutError(hasError = false)

    doc.createTestsWithSubmissionButton(
      action = routes.CertificateCheckYourAnswersController.onSubmit(),
      buttonText = "Continue"
    )
  }
}

object CertificateCheckYourAnswersViewSpec {
  val pageHeading = "certificateCheckYourAnswers"
  val pageTitle   = "certificateCheckYourAnswers"
}
