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
import models.{UpscanFileReference, UpscanInitiateResponse}
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

    "must contain hidden fields" in {
      val hiddenFields = doc.select("input[type=hidden]")
      hiddenFields.size() mustBe 2
      hiddenFields.get(0).attr("name") mustBe "test1"
      hiddenFields.get(1).attr("name") mustBe "test2"
      hiddenFields.get(0).attr("value") mustBe "testValue1"
      hiddenFields.get(1).attr("value") mustBe "testValue2"
    }
  }
}

object NotificationUploadFormViewSpec {
  val pageHeading                                    = "notificationUploadForm"
  val pageTitle                                      = "notificationUploadForm"
  val upscanInitiateResponse: UpscanInitiateResponse = UpscanInitiateResponse(
    fileReference = UpscanFileReference("testReference"),
    postTarget = "formPostTarget",
    formFields = Map(
      "test1" -> "testValue1",
      "test2" -> "testValue2"
    )
  )
}
