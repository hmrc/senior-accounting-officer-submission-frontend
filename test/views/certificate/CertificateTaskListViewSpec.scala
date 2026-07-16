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
import controllers.certificate.routes as certificateRoutes
import models.certificate.CertificateTaskListState
import models.{NormalMode, TaskStatus}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.certificate.CertificateTaskListView

import CertificateTaskListViewSpec.*

class CertificateTaskListViewSpec extends ViewSpecBase[CertificateTaskListView] {

  private def generateView(state: CertificateTaskListState): Document = Jsoup.parse(SUT(state).toString)

  "CertificateTaskListView" - {

    for {
      provideSaoDetailsStageStatus   <- TaskStatus.values
      uploadSubmissionTemplateStatus <- TaskStatus.values
      submitCertificateStageStatus   <- TaskStatus.values
      showButton                     <- Seq(true, false)
    } {

      s"provide the sao details stage is in the $provideSaoDetailsStageStatus state, upload the submission template stage is in the $uploadSubmissionTemplateStatus state, submit the certificate stage is in the $submitCertificateStageStatus state and the button will be $showButton" - {
        val state = CertificateTaskListState(
          provideSaoDetailsStage = provideSaoDetailsStageStatus,
          uploadSubmissionTemplateStage = uploadSubmissionTemplateStatus,
          submitCertificateStage = submitCertificateStageStatus,
          showContinueButton = showButton
        )

        val doc: Document = generateView(state)

        doc.createTestsWithStandardPageElements(
          pageTitle = pageTitle,
          pageHeading = pageHeading,
          showBackLink = true,
          showIsThisPageNotWorkingProperlyLink = true,
          hasError = false
        )

        doc.createTestsWithOrWithoutError(hasError = false)

        doc.createTestsWithParagraphs(paragraphs)

        doc.createTestsWithTaskList(expectedState = state)
      }
    }
  }

  extension (doc: Document) {
    def createTestsWithTaskList(expectedState: CertificateTaskListState): Unit = {
      "must render task list data-test-ids" in {
        val taskNames = doc.getMainContent.getElementsByClass("govuk-task-list__name-and-hint")
        taskNames.size() mustBe 3

        taskNames.get(0).attr("data-test-id") mustBe "provide-sao-details"
        taskNames.get(1).attr("data-test-id") mustBe "upload-submission-template"
        taskNames.get(2).attr("data-test-id") mustBe "submit-certificate"
      }

      "must render task list status ids" in {
        val statusTags = doc.getMainContent.getElementsByClass("govuk-task-list__status")
        statusTags.size() mustBe 3

        statusTags.get(0).id() mustBe "provide-sao-details-status"
        statusTags.get(1).id() mustBe "upload-submission-template-status"
        statusTags.get(2).id() mustBe "submit-certificate-status"
      }

      if expectedState.provideSaoDetailsStage == TaskStatus.NotStarted then {
        doc.getMainContent
          .select("""[data-test-id="provide-sao-details"] a.govuk-link""")
          .get(0)
          .createTestWithLink(
            linkText = provideSaoDetailsLinkText,
            destinationUrl = certificateRoutes.CertificateSaoFullNameController.onPageLoad(NormalMode).url
          )
      }

      if expectedState.uploadSubmissionTemplateStage == TaskStatus.NotStarted then {
        doc.getMainContent
          .select("""[data-test-id="upload-submission-template"] a.govuk-link""")
          .get(0)
          .createTestWithLink(
            linkText = uploadSubmissionTemplateLinkText,
            destinationUrl = certificateRoutes.CertificateUploadFormController.onPageLoad().url
          )
      }

      if expectedState.submitCertificateStage == TaskStatus.NotStarted then {
        doc.getMainContent
          .select("""[data-test-id="submit-certificate"] a.govuk-link""")
          .get(0)
          .createTestWithLink(
            linkText = submitCertificateLinkText,
            destinationUrl = certificateRoutes.CertificateAdditionalInformationController.onPageLoad(NormalMode).url
          )
      }

      "must show the correct statuses" in {
        val statusTags = doc.getMainContent.getElementsByClass("govuk-task-list__status")
        statusTags.size() mustBe 3

        val provideSaoDetailsTag        = statusTags.get(0)
        val uploadSubmissionTemplateTag = statusTags.get(1)
        val submitCertificateTag        = statusTags.get(2)

        expectedState.provideSaoDetailsStage match {
          case TaskStatus.CannotStartYet => provideSaoDetailsTag.text() mustBe cannotStartText
          case TaskStatus.NotStarted     => {
            provideSaoDetailsTag.text() mustBe notStartedText
            provideSaoDetailsTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--blue"
          }
          case TaskStatus.Completed => provideSaoDetailsTag.text() mustBe completedText
        }

        expectedState.uploadSubmissionTemplateStage match {
          case TaskStatus.CannotStartYet => uploadSubmissionTemplateTag.text() mustBe cannotStartText
          case TaskStatus.NotStarted     => {
            uploadSubmissionTemplateTag.text() mustBe notStartedText
            uploadSubmissionTemplateTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--blue"
          }
          case TaskStatus.Completed => uploadSubmissionTemplateTag.text() mustBe completedText
        }

        expectedState.submitCertificateStage match {
          case TaskStatus.CannotStartYet => submitCertificateTag.text() mustBe cannotStartText
          case TaskStatus.NotStarted     => {
            submitCertificateTag.text() mustBe notStartedText
            submitCertificateTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--blue"
          }
          case TaskStatus.Completed => submitCertificateTag.text() mustBe completedText
        }
      }

      if expectedState.showContinueButton then {
        doc.createTestsWithSubmissionButton(certificateRoutes.CertificateTaskListController.onSubmit(), pageButtonText)
      } else {
        s"must not have a form" in {
          val form = doc.select("form")
          withClue(
            s"Form found\n"
          ) {
            form.size() mustBe 0
          }
        }

        s"must not have a submit button" in {
          val button = doc.select("button[type=submit]")
          withClue(
            s"Submit Button found\n"
          ) {
            button.size() mustBe 0
          }
        }
      }
    }
  }
}

object CertificateTaskListViewSpec {
  val pageHeading    = "Submit a certificate"
  val pageTitle      = "Submit a certificate"
  val pageButtonText = "Go back to the homepage"

  val paragraphs: Seq[String] = Seq(
    "Submit a certificate and confirm who is responsible for the group’s tax accounting arrangements for the financial year."
  )

  val provideSaoDetailsLinkText        = "Provide the SAO’s details"
  val uploadSubmissionTemplateLinkText = "Upload the submission template"
  val submitCertificateLinkText        = "Submit the certificate"

  val notStartedText  = "Not started"
  val cannotStartText = "Cannot start yet"
  val completedText   = "Completed"
}
