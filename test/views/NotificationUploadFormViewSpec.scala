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
import models.UpscanInitiateResponse
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.NotificationUploadFormViewSpec.*
import views.html.NotificationUploadFormView

class NotificationUploadFormViewSpec extends ViewSpecBase[NotificationUploadFormView] {

  private def generateView(): Document = Jsoup.parse(SUT(upscanInitiateResponse).toString)

  "NotificationUploadFormView" - {
    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = true,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithOrWithoutError(hasError = false)

    "must contain hidden fields" in {
      val hiddenFields = doc.select("input[type=hidden]")
      hiddenFields.size() mustBe 2
      hiddenFields.get(0).attr("name") mustBe "test1"
      hiddenFields.get(1).attr("name") mustBe "test2"
      hiddenFields.get(0).attr("value") mustBe "testValue1"
      hiddenFields.get(1).attr("value") mustBe "testValue2"
    }

    doc.createTestsWithParagraphs(paragraphs)

    doc.createTestForInsetText(insetText)

    "must contain file upload input element" in {
      doc.select(s"input#$uploadFormInputId.govuk-file-upload").size() mustBe 1
    }

    "must contain label for file upload input element" in {
      doc.select(s"""label.govuk-label[for="$uploadFormInputId"]""").size() mustBe 1
      doc.select(s"""label.govuk-label[for="$uploadFormInputId"]""").text() mustBe uploadFormLabel
    }
  }
}

object NotificationUploadFormViewSpec {
  val pageHeading = "Upload a submission template"
  val pageTitle   = "Upload a submission template for your notification"

  val paragraphs = List(
    "The template must be uploaded in CSV format. If you already have the template saved in a different format (such as .xls or .xlsx), you must save it again as a CSV file.",
    "Read guidance on how to complete the submission template (opens in new tab)"
  )
  val insetText =
    "If your group has more than one SAO, you must submit a separate notification for each SAO. After you complete one submission, you can start another."

  val uploadFormLabel   = "Upload a file"
  val uploadFormInputId = "file-input"

  val upscanInitiateResponse: UpscanInitiateResponse = UpscanInitiateResponse(
    reference = "testReference",
    postTarget = "formPostTarget",
    formFields = Map(
      "test1" -> "testValue1",
      "test2" -> "testValue2"
    )
  )
}
