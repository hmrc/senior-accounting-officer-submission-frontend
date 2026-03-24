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

package viewmodels.checkAnswers

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import views.html.CertificateCheckYourAnswersView

trait CheckYourAnswersSummaryRenderingSupport extends SpecBase with GuiceOneAppPerSuite {
  override def fakeApplication(): Application = applicationBuilder().build()

  given Request[?] = FakeRequest()
  given Messages   = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  private lazy val certificateCheckYourAnswersView = app.injector.instanceOf[CertificateCheckYourAnswersView]

  protected def renderSummaryRow(row: SummaryListRow): Element =
    Jsoup
      .parse(certificateCheckYourAnswersView(SummaryList(rows = Seq(row))).toString)
      .select(".govuk-summary-list__row")
      .first()

  extension (row: Element) {
    def renderedKeyText: String     = row.select("dt.govuk-summary-list__key").text()
    def renderedValueText: String   = row.select("dd.govuk-summary-list__value").text()
    def renderedValueHtml: String   = row.select("dd.govuk-summary-list__value").html()
    def renderedActionLink: Element = row.select("dd.govuk-summary-list__actions a").first()
  }
}
