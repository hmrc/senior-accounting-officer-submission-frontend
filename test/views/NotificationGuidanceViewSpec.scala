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

import base.SpecBase
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.NotificationGuidanceView

class NotificationGuidanceViewSpec extends SpecBase with GuiceOneAppPerSuite {

  val SUT: NotificationGuidanceView = app.injector.instanceOf[NotificationGuidanceView]

  given request: Request[_] = FakeRequest()

  given Messages = app.injector.instanceOf[MessagesApi].preferred(request)

  val doc         = Jsoup.parse(SUT().toString)
  val mainContent = doc.getElementById("main-content")

  "NotificationGuidanceView" must {
//    "must generate a view with the correct heading" in {
//      val h1 = mainContent.getElementsByTag("h1")
//      h1.size() mustBe 1
//      h1.get(0).text() mustBe "Notification template guide"
//    }

    //      "with the correct paragraphs" in {
    //        val mainContent = doc.getElementById("main-content")
    //
    //        val paragraphs = mainContent.getElementsByTag("p")
    //        paragraphs.size() mustBe 4
    //        List.from(paragraphs.iterator().asScala).foreach(p => p.attr("class") mustBe "govuk-body")
    //
    //        paragraphs
    //          .get(0)
    //          .text() mustBe "Register the company responsible for submitting the Senior Accounting Officer (SAO) notification and certificate. There’s no required company type. This should be the company where the SAO works from."
    //        paragraphs
    //          .get(1)
    //          .text() mustBe "If your group has more than one SAO, you’ll need to complete a separate registration for each SAO."
    //        paragraphs
    //          .get(2)
    //          .text() mustBe "At the Review and submit section of this registration, you can amend your answers and print or save them for your own records."
    //        paragraphs.get(3).text() mustBe "Is this page not working properly? (opens in new tab)"
    //      }
    //
    //      "with the correct subheadings and content" in {
    //        val mainContent = doc.getElementById("main-content")
    //
    //        val headings2 = mainContent.getElementsByTag("h2")
    //        headings2.size() mustBe 3
    //
    //        headings2.asScala.foreach(h2 => h2.attr("class") mustBe "govuk-heading-m")
    //        headings2.get(0).text() mustBe "Company details"
    //        headings2.get(1).text() mustBe "Contact details"
    //        headings2.get(2).text() mustBe "Review and submit"
    //      }
    //
    //      "with the correct link texts" in {
    //        val mainContent = doc.getElementById("main-content")
    //
    //        val links = mainContent.getElementsByClass("govuk-link govuk-task-list__link")
    //        links.size() mustBe 3
    //        links.get(0).text() mustBe "Company details"
    //        links.get(1).text() mustBe "Contact details"
    //        links.get(2).text() mustBe "Check your answers before submitting your registration"
    //      }
    //
    //      "with the correct label texts" in {
    //        val mainContent = doc.getElementById("main-content")
    //        val statusTags  = mainContent.getElementsByClass("govuk-task-list__status")
    //
    //        statusTags.size() mustBe 3
    //
    //        statusTags.asScala.foreach(tag =>
    //          tag.text() mustBe "Completed"
    //          tag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--green"
    //        )
    //      }
    //
    //      "must not show a back link" in {
    //        val backLink = doc.getElementsByClass("govuk-back-link")
    //        backLink.size() mustBe 0
    //      }
    //
    //      "must show help link" in {
    //        val mainContent = doc.getElementById("main-content")
    //
    //        val helpLink = mainContent.getElementsByClass("govuk-link hmrc-report-technical-issue ")
    //        helpLink.size() mustBe 1
    //      }
  }
}
