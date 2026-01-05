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
import models.SubmitNotificationStage
import models.SubmitNotificationStage.{SubmitNotificationInfo, UploadSubmissionTemplateDetails}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.SubmitNotificationStartViewSpec.*
import views.html.SubmitNotificationStartView

class SubmitNotificationStartViewSpec extends ViewSpecBase[SubmitNotificationStartView] {

  private def generateView(stage: SubmitNotificationStage): Document = Jsoup.parse(SUT(stage).toString)

  "SubmitNotificationStartView" - {

    "SubmitNotificationStartView with 'upload template' stage" - {

      val doc: Document = generateView(UploadSubmissionTemplateDetails)
      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = false,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithParagraphs(paragraphs)

      doc.createTestsWithOrWithoutError(hasError = false)

      doc.getMainContent
        .select("a.govuk-link")
        .get(0)
        .createTestWithLink(
          linkText = guidanceLinkText,
          destinationUrl = routes.NotificationGuidanceController.onPageLoad().url
        )

      doc.getMainContent
        .select("a.govuk-link")
        .get(1)
        .createTestWithLink(
          linkText = uploadTemplateLinkText,
          destinationUrl = routes.NotificationUploadFormController.onPageLoad().url
        )

      "must show the correct statuses" in {
        val statusTags = doc.getMainContent.getElementsByClass("govuk-task-list__status")
        statusTags.size() mustBe 2

        val uploadNotificationTag = statusTags.get(0)
        val submitNotificationTag = statusTags.get(1)

        uploadNotificationTag.text() mustBe "Not started"
        uploadNotificationTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--blue"
        submitNotificationTag.text() mustBe "Cannot start yet"
        submitNotificationTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--grey"
      }
    }

    "SubmitNotificationStartView with 'submit notification' stage" - {
      val doc: Document = generateView(SubmitNotificationInfo)
      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = false,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithParagraphs(paragraphs)

      doc.createTestsWithOrWithoutError(hasError = false)

      "must have link to template guidance" - {
        "link must open in a new tab" in {
          doc.getMainContent
            .select("a.govuk-link")
            .get(0)
            .attr("target") mustBe "_blank"
        }

        doc.getMainContent
          .select("a.govuk-link")
          .get(0)
          .createTestWithLink(
            linkText = guidanceLinkText,
            destinationUrl = routes.NotificationGuidanceController.onPageLoad().url
          )
      }

      doc.getMainContent
        .select("a.govuk-link")
        .get(1)
        .createTestWithLink(
          linkText = submitNotificationLinkText,
          destinationUrl = routes.NotificationGuidanceController.onPageLoad().url
        )

      "must show the correct statuses" in {
        val statusTags = doc.getMainContent.getElementsByClass("govuk-task-list__status")
        statusTags.size() mustBe 2

        val uploadNotificationTag = statusTags.get(0)
        val submitNotificationTag = statusTags.get(1)

        uploadNotificationTag.text() mustBe "Completed"
        uploadNotificationTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--green"
        submitNotificationTag.text() mustBe "Not started"
        submitNotificationTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--blue"
      }
    }
  }
}

object SubmitNotificationStartViewSpec {
  val pageHeading = "Submit a notification"
  val pageTitle   = "Submit a notification"
  val paragraphs  = Seq(
    "To submit a notification, you’ll need to:",
    "If you need help, read the submission template guidance (opens in new tab)."
  )
  val guidanceLinkText           = "read the submission template guidance (opens in new tab)"
  val uploadTemplateLinkText     = "Upload a submission template"
  val submitNotificationLinkText = "Submit a notification"

}
