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

package views.combined

import base.ViewSpecBase
import controllers.combined.provideSaoDetails.routes as provideSaoDetailsRoutes
import controllers.combined.routes as combinedRoutes
import controllers.combined.submitCertificate.routes as submitCertificateRoutes
import controllers.combined.submitNotification.routes as submitNotificationRoutes
import controllers.combined.uploadTemplate.routes as uploadTemplateRoutes
import models.NormalMode
import models.combined.CombinedStage
import models.combined.CombinedStage.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.combined.CombinedTaskListViewSpec.*
import views.html.combined.CombinedTaskListView

class CombinedTaskListViewSpec extends ViewSpecBase[CombinedTaskListView] {

  private def generateView(stage: CombinedStage): Document = Jsoup.parse(SUT(stage).toString)

  "CombinedTaskListView" - {

    "CombinedTaskListView with 'provide sao details' stage" - {

      val doc: Document = generateView(CombinedStage.ProvideSaoDetails)
      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = false,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithParagraphs(paragraphs)

      doc.createTestsWithOrWithoutError(hasError = false)

      "must render task list data-test-ids" in {
        val taskNames = doc.getMainContent.getElementsByClass("govuk-task-list__name-and-hint")
        taskNames.size() mustBe 4

        taskNames.get(0).attr("data-test-id") mustBe "provide-sao-details"
        taskNames.get(1).attr("data-test-id") mustBe "upload-submission-template"
        taskNames.get(2).attr("data-test-id") mustBe "submit-notification"
        taskNames.get(3).attr("data-test-id") mustBe "submit-certificate"
      }

      "must render task list status ids" in {
        val statusTags = doc.getMainContent.getElementsByClass("govuk-task-list__status")
        statusTags.size() mustBe 4

        statusTags.get(0).id() mustBe "provide-sao-details-status"
        statusTags.get(1).id() mustBe "upload-template-details-status"
        statusTags.get(2).id() mustBe "submit-notification-details-status"
        statusTags.get(3).id() mustBe "submit-certificate-details-status"
      }

      doc.getMainContent
        .select("a.govuk-link")
        .get(0)
        .createTestWithLink(
          linkTestDescription = "provide SAO details",
          linkText = provideSaoDetailsLinkText,
          destinationUrl = provideSaoDetailsRoutes.CombinedMoreThanOneSaoController.onPageLoad(NormalMode).url,
          isExternalLink = false
        )

      "must show the correct statuses" in {
        val statusTags = doc.getMainContent.getElementsByClass("govuk-task-list__status")
        statusTags.size() mustBe 4

        val provideSaoDetailsTag  = statusTags.get(0)
        val uploadNotificationTag = statusTags.get(1)
        val submitNotificationTag = statusTags.get(2)
        val submitCertificateTag  = statusTags.get(3)

        provideSaoDetailsTag.text() mustBe notStartedText
        provideSaoDetailsTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--blue"
        uploadNotificationTag.text() mustBe cannotStartText
        submitNotificationTag.text() mustBe cannotStartText
        submitCertificateTag.text() mustBe cannotStartText
      }
    }

    "CombinedTaskListView with 'upload template' stage" - {

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
          linkTestDescription = "provide SAO details",
          linkText = provideSaoDetailsLinkText,
          destinationUrl = provideSaoDetailsRoutes.CombinedMoreThanOneSaoController.onPageLoad(NormalMode).url,
          isExternalLink = false
        )

      doc.getMainContent
        .select("a.govuk-link")
        .get(1)
        .createTestWithLink(
          linkTestDescription = "upload template",
          linkText = uploadTemplateLinkText,
          destinationUrl = uploadTemplateRoutes.CombinedUploadFormController.onPageLoad().url,
          isExternalLink = false
        )

      "must show the correct statuses" in {
        val statusTags = doc.getMainContent.getElementsByClass("govuk-task-list__status")
        statusTags.size() mustBe 4

        val provideSaoDetailsTag  = statusTags.get(0)
        val uploadNotificationTag = statusTags.get(1)
        val submitNotificationTag = statusTags.get(2)
        val submitCertificateTag  = statusTags.get(3)

        provideSaoDetailsTag.text() mustBe completedText
        uploadNotificationTag.text() mustBe notStartedText
        uploadNotificationTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--blue"
        submitNotificationTag.text() mustBe cannotStartText
        submitCertificateTag.text() mustBe cannotStartText
      }
    }

    "CombinedTaskListView with 'submit notification' stage" - {
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

      doc.getMainContent
        .select("a.govuk-link")
        .get(0)
        .createTestWithLink(
          linkTestDescription = "provide SAO details",
          linkText = provideSaoDetailsLinkText,
          destinationUrl = provideSaoDetailsRoutes.CombinedMoreThanOneSaoController.onPageLoad(NormalMode).url,
          isExternalLink = false
        )

      doc.getMainContent
        .select("a.govuk-link")
        .get(1)
        .createTestWithLink(
          linkTestDescription = "upload template",
          linkText = uploadTemplateLinkText,
          destinationUrl = uploadTemplateRoutes.CombinedUploadFormController.onPageLoad().url,
          isExternalLink = false
        )

      doc.getMainContent
        .select("a.govuk-link")
        .get(2)
        .createTestWithLink(
          linkTestDescription = "submit notification",
          linkText = submitNotificationLinkText,
          destinationUrl =
            submitNotificationRoutes.CombinedNotificationAdditionalInformationController.onPageLoad(NormalMode).url,
          isExternalLink = false
        )

      "must show the correct statuses" in {
        val statusTags = doc.getMainContent.getElementsByClass("govuk-task-list__status")
        statusTags.size() mustBe 4

        val provideSaoDetailsTag  = statusTags.get(0)
        val uploadNotificationTag = statusTags.get(1)
        val submitNotificationTag = statusTags.get(2)
        val submitCertificateTag  = statusTags.get(3)

        provideSaoDetailsTag.text() mustBe completedText
        uploadNotificationTag.text() mustBe completedText
        submitNotificationTag.text() mustBe notStartedText
        submitNotificationTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--blue"
        submitCertificateTag.text() mustBe cannotStartText
      }
    }

    "CombinedTaskListView with 'submit certificate' stage" - {
      val doc: Document = generateView(SubmitCertificateInfo)
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
          linkTestDescription = "provide SAO details",
          linkText = provideSaoDetailsLinkText,
          destinationUrl = provideSaoDetailsRoutes.CombinedMoreThanOneSaoController.onPageLoad(NormalMode).url,
          isExternalLink = false
        )

      doc.getMainContent
        .select("a.govuk-link")
        .get(1)
        .createTestWithLink(
          linkTestDescription = "upload template",
          linkText = uploadTemplateLinkText,
          destinationUrl = uploadTemplateRoutes.CombinedUploadFormController.onPageLoad().url,
          isExternalLink = false
        )

      doc.getMainContent
        .select("a.govuk-link")
        .get(2)
        .createTestWithLink(
          linkTestDescription = "submit notification",
          linkText = submitNotificationLinkText,
          destinationUrl =
            submitNotificationRoutes.CombinedNotificationAdditionalInformationController.onPageLoad(NormalMode).url,
          isExternalLink = false
        )

      doc.getMainContent
        .select("a.govuk-link")
        .get(3)
        .createTestWithLink(
          linkTestDescription = "submit certificate",
          linkText = submitCertificateLinkText,
          destinationUrl = submitCertificateRoutes.CombinedCertificateSaoFullNameController.onPageLoad(NormalMode).url,
          isExternalLink = false
        )

      "must show the correct statuses" in {
        val statusTags = doc.getMainContent.getElementsByClass("govuk-task-list__status")
        statusTags.size() mustBe 4

        val provideSaoDetailsTag  = statusTags.get(0)
        val uploadNotificationTag = statusTags.get(1)
        val submitNotificationTag = statusTags.get(2)
        val submitCertificateTag  = statusTags.get(3)

        provideSaoDetailsTag.text() mustBe completedText
        uploadNotificationTag.text() mustBe completedText
        submitNotificationTag.text() mustBe completedText
        submitCertificateTag.text() mustBe notStartedText
        submitCertificateTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--blue"
      }
    }
  }

  "CombinedTaskListView with all stages complete" - {
    val doc: Document = generateView(AllStagesCompleted)
    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = false,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithParagraphs(paragraphs)

    doc.createTestsWithOrWithoutError(hasError = false)

    "must show the correct statuses" in {
      val statusTags = doc.getMainContent.getElementsByClass("govuk-task-list__status")
      statusTags.size() mustBe 4

      val provideSaoDetailsTag  = statusTags.get(0)
      val uploadNotificationTag = statusTags.get(1)
      val submitNotificationTag = statusTags.get(2)
      val submitCertificateTag  = statusTags.get(3)

      provideSaoDetailsTag.text() mustBe completedText
      uploadNotificationTag.text() mustBe completedText
      submitNotificationTag.text() mustBe completedText
      submitCertificateTag.text() mustBe completedText
    }

    doc.createTestsWithSubmissionButton(
      action = combinedRoutes.CombinedTaskListController.onCompleteSubmit,
      buttonText = goBackToHomepage
    )
  }
}

object CombinedTaskListViewSpec {
  val pageHeading             = "Submit a notification and certificate"
  val pageTitle               = "Submit a notification and certificate"
  val paragraphs: Seq[String] = Seq(
    "Submit a notification and certificate for each SAO in your organisation. The notification must include the SAO’s name and all the companies they were responsible for. The certificate confirms whether your company’s tax accounting arrangements were appropriate for a previous financial year."
  )
  val provideSaoDetailsLinkText  = "Provide the SAO’s details"
  val uploadTemplateLinkText     = "Upload the submission template"
  val submitNotificationLinkText = "Submit the notification"
  val submitCertificateLinkText  = "Submit the certificate"

  val notStartedText   = "Not started"
  val cannotStartText  = "Cannot start yet"
  val completedText    = "Completed"
  val goBackToHomepage = "Go back to the homepage"
}
