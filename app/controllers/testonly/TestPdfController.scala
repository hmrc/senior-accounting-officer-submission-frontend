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

package controllers.testonly

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import controllers.testonly.OpenHtmlToPdfService.*
import org.apache.pdfbox.pdmodel.*
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Source, StreamConverters}
import org.apache.pekko.util.ByteString
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future, blocking}

import java.io.{File, PipedInputStream, PipedOutputStream}
import java.util.GregorianCalendar
import javax.inject.Inject

class TestPdfController @Inject() (
    mcc: MessagesControllerComponents,
    openHtmlToPdfService: OpenHtmlToPdfService
)(implicit val ec: ExecutionContext, system: ActorSystem)
    extends FrontendController(mcc)
    with I18nSupport {

  def testPdf(): Action[AnyContent] = Action { implicit request =>
    val document = openHtmlToPdfService.testPdf()

    val dataContent = document.asSource
    Ok.chunked(dataContent, inline = false, fileName = Some("test.pdf"))
  }

}

class OpenHtmlToPdfService {
  def testPdf(): PdfRendererBuilder = {

    val builder = new PdfRendererBuilder
    builder.withProducer("HMRC forms services")

    // 1. Enable PDF/UA-1 conformance and tagging
    // Core Conformance Levels
    //
    //    Level a (Accessible): Full compliance, including document structure and tags for accessibility.
    //    Level b (Basic): Ensures visual integrity (appearance) over the long term.
    //    Level u (Unicode): Requires text to be mapped to Unicode, ensuring reliable searching and copying.
    //
    // PDF/A Versions
    //
    //    PDF/A-1: Based on PDF 1.4; defines minimum requirements for archival.
    //    PDF/A-2: Based on PDF 1.7; supports JPEG2000, transparency, and PDF/A file embedding.
    //    PDF/A-3: Allows embedding arbitrary file formats (e.g., XML, CSV).
    //    PDF/A-4: Aligns with PDF 2.0; includes support for interactive forms and annotations.
    builder.usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_3_A)
    builder.usePdfUaAccessibility(true)

    builder.useFont(
      File(this.getClass.getResource("/pdf/fonts/Helvetica.ttf").getFile),
      "Helvetica"
    )

    // 2. Point to your source (file, URI, or string)
    builder.withHtmlContent(
      OpenHtmlToPdfService.testHtml,
      this.getClass.getResource("/pdf/").toURI.toString
    )

    builder
  }
}

object OpenHtmlToPdfService {

  def tables(data: Seq[Notification.Row]): String = {
    s"""
       | <table>
       |  <caption>Notification Details</caption>
       |  <thead>
       |    <tr><th colspan="6"><h2>Submissions<span class="continued"></span></h2></th></tr>
       |    <tr>
       |      <th>Company</th>
       |      <th>UTR</th>
       |      <th>CRN</th>
       |      <th>Company type</th>
       |      <th>Company status</th>
       |      <th>Financial year end date</th>
       |    </tr>
       |  </thead>
       |  ${data.foldLeft("")((concat, row) =>
        s"""
                                                  | $concat
                                                  |<tbody>
                                                  |  <tr>
                                                  |    <td>${row.companyName}</td>
                                                  |    <td>${row.crn}</td>
                                                  |    <td>${row.utr}</td>
                                                  |    <td>${row.companyType}</td>
                                                  |    <td>${row.status}</td>
                                                  |    <td>${row.financialYearEndDate}</td>
                                                  |  </tr>
                                                  |</tbody>
                                                  |""".stripMargin
      )}
       | </table>""".stripMargin
  }

  // <html lang="en-GB"> changes the PDF language metadata, otherwise it's EN-US by default
  def testHtml: String = {
    s"""
       |<!DOCTYPE html>
       |<html lang="en-GB">
       |<head>
       |<title>SAO Notification Confirmation</title>
       |<meta name="author" content="HMRC forms service"/>
       |<meta name="subject" content="SAO Notification Confirmation"/>
       |<meta name="Creator" content="HMRC forms service"/>
       |<style>
       |  /* https://github.com/openhtmltopdf/openhtmltopdf/wiki/Cut-off-page-support */
       |@page {
       |  size: A4;
       |  margin-top: 2cm; /* Ensure enough space for the header */
       |  -fs-max-overflow-pages: 10; /* 0 by default */
       |  -fs-overflow-pages-direction: ltr; /* Also available is rtl */
       |  @top-left {
       |    content: element(header)
       |  }
       |  @top-right {
       |    /* Note the use of the -fs-if-cut-off function below. */
       |    content: "Page " counter(page) -fs-if-cut-off(" (continued)") " of " counter(pages);
       |    font-family: Helvetica, sans-serif;
       |    font-size: 16px;
       |  }
       |}
       |.logo-container {
       |    position: running(header); /* Names this element 'header' */
       |    width: 100%;
       |    text-align: left;
       |}
       |.logo {
       |   height: 2em;
       |   vertical-align: middle;
       |}
       |.logo-text {
       |    font-size: 12pt;
       |    font-weight: bold;
       |    vertical-align: middle;
       |}
       |
       |body {
       |  font-family: Helvetica, sans-serif;
       |}
       |table {
       |  border-collapse: collapse;
       |  /* hack: width: 100%; doesn't seem to work for a named page turned to landscape,
       |   it seems to still pick up the width from the main page level
       |   hard coding the desired width */
       |  width: 268mm;
       |
       |  /* https://github.com/openhtmltopdf/openhtmltopdf/wiki/Custom-CSS-properties */
       |  -fs-table-paginate: paginate;
       |  -fs-page-break-min-height: 1.5cm;
       |}
       |.continued:before {
       |    /**
       |     * For repeated table headers (thead elements), show " - Continued"
       |     * before each table header repeat on subsequent (not initial) pages.
       |     * See https://github.com/danfickle/openhtmltopdf/pull/32
       |     */
       |    content: " - Continued";
       |    visibility: -fs-table-paginate-repeated-visible;
       |}
       |
       |tr, thead, tfoot {
       |  page-break-inside: avoid;
       |}
       |
       |td, th {
       |  border: 1px solid #ddd;
       |  padding: 8px;
       |}
       |
       |tbody:nth-child(even){background-color: #f2f2f2;}
       |
       |tr:hover {background-color: #ddd;}
       |
       |th {
       |  padding-top: 12px;
       |  padding-bottom: 12px;
       |  text-align: left;
       |  background-color: black;
       |  color: white;
       |}
       |
       |
       |@page tables {
       |  size: A4 landscape;
       |}
       |
       |tables-page {
       |  page: tables;
       |  page-break-before: always;
       |  page-break-inside: avoid;
       |  /* The page property allows us to marry up an element with a @page rule. */
       |  display: block;
       |}
       |
       |</style>
       |</head>
       |<body>
       |<div class="logo-container">
       |  <img class="logo" src="gov-uk-logo.png" alt="GOV.UK"/>
       |  <div class="logo-text">Senior Accounting Officer notification and certificate</div>
       |</div>
       |<bookmarks>
       | <bookmark name="This is a Heading" href="#heading"/>
       | <bookmark name="Appendix A: Tables" href="#tables"/>
       |</bookmarks>
       |
       |<h1 id="heading">This is a Heading</h1>
       |<p>This is a paragraph.</p>
       |
       |<tables-page>
       |  <h1 id="tables">Appendix A: Tables</h1>
       |  ${tables(TestPdfController.genTestCompanies(30))}
       |</tables-page>
       |
       |</body>
       |</html>
       |""".stripMargin
  }

  extension (document: PDDocument) {

    def setMetaData(date: GregorianCalendar) = {
      val pdd = document.getDocumentInformation
      pdd.setAuthor("HMRC")
      pdd.setTitle("SAO Notification")
      pdd.setCreator("HMRC")
      pdd.setSubject("SAO Notification Confirmation")
      pdd.setCreationDate(date)
      pdd.setModificationDate(date)
      pdd.setKeywords("sample notification pdf")
    }

    def asSource(using system: ActorSystem): Source[ByteString, ?] = StreamConverters.fromInputStream(() => {
      given blockingEc: ExecutionContext =
        system.dispatchers.lookup("pekko.stream.materializer.blocking-io-dispatcher")

      val pos = new PipedOutputStream
      Future {
        blocking {
          document.save(pos)
        }
      }.onComplete { _ =>
        pos.close()
        document.close()
      }
      new PipedInputStream(pos)
    })
  }

  extension (builder: PdfRendererBuilder) {
    def asSource(using system: ActorSystem): Source[ByteString, ?] = StreamConverters
      .fromInputStream(() => {
        given blockingEc: ExecutionContext =
          system.dispatchers.lookup("pekko.stream.materializer.blocking-io-dispatcher")

        val pos = new PipedOutputStream

        // 3. Set the output stream
        builder.toStream(pos)

        Future {
          blocking {
            // 4. Run the conversion
            builder.run()
          }
        }.onComplete { _ =>
          pos.close()
        }
        new PipedInputStream(pos)
      })
  }
}

object TestPdfController {
  val logger: Logger = Logger(TestPdfController.getClass)

  def genTestCompanies(total: Int): Seq[Notification.Row] = {
    (1 to total).map {
      case index if (index & 1) == 0 =>
        Notification.Row(
          companyName =
            s"TEST NAME OF THE COMPANY WITH THE LONGEST NAME SO FAR INCORPORATED AT THE REGISTRY OF COMPANIES IN ENGLAND AND WALES AND ENCOMPASSING THE REGISTRIES BASED IN SCOTLAND $index",
          utr = "0123456789",
          crn = "9876543210",
          companyType = "LTD",
          status = "Administration",
          financialYearEndDate = "31/01/2025"
        )
      case index =>
        Notification.Row(
          companyName = s"Company $index",
          utr = "0123456789",
          crn = "9876543210",
          companyType = "PLC",
          status = "Active",
          financialYearEndDate = "31/01/2025"
        )
    }
  }

}

object Notification {
  final case class Row(
      companyName: String,
      utr: String,
      crn: String,
      companyType: "PLC" | "LTD",
      status: "Active" | "Dormant" | "Administration" | "Liquidation",
      financialYearEndDate: String
  )
}
