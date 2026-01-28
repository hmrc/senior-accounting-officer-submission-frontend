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

import com.openhtmltopdf.pdfboxout.{PdfBoxRenderer, PdfRendererBuilder}
import controllers.testonly.TestPdfController.*
import org.apache.pdfbox.cos.*
import org.apache.pdfbox.pdmodel.*
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.*
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.*
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.*
import org.apache.pdfbox.pdmodel.encryption.{AccessPermission, StandardProtectionPolicy}
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.IOResult
import org.apache.pekko.stream.scaladsl.{Source, StreamConverters}
import org.apache.pekko.util.ByteString
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.io.{File, PipedInputStream, PipedOutputStream}
import java.util.{Calendar, GregorianCalendar}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future, blocking}

class TestPdfController @Inject() (
    mcc: MessagesControllerComponents,
    pdfBoxService: PdfBoxService,
    openHtmlToPdfService: OpenHtmlToPdfService
)(implicit val ec: ExecutionContext, system: ActorSystem)
    extends FrontendController(mcc)
    with I18nSupport {

  def testPdf(): Action[AnyContent] = Action { implicit request =>
//    val document = pdfBoxService.temp2()
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
      TestPdfController.testHtml,
      this.getClass.getResource("/pdf/").toURI.toString
    )

    builder
  }
}

class PdfBoxService {
  def testRawOdfBoxAttempt3(): PDDocument = {
    import org.apache.pdfbox.cos.{COSDictionary, COSName}
    import org.apache.pdfbox.pdmodel.*
    import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.*
    import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.StandardStructureTypes
    import org.apache.pdfbox.pdmodel.font.PDType1Font

    def taggedTableExample(): PDDocument = {
      val doc = new PDDocument
      try {
        val page = new PDPage
        doc.addPage(page)
        // 1. Setup Structure Tree Root
        val treeRoot = new PDStructureTreeRoot
        doc.getDocumentCatalog.setStructureTreeRoot(treeRoot)
        doc.getDocumentCatalog.setMarkInfo(new PDMarkInfo)
        doc.getDocumentCatalog.getMarkInfo.setMarked(true)
        // 2. Create the Table Structure Element
        val table = new PDStructureElement(StandardStructureTypes.TABLE, treeRoot)
        treeRoot.appendKid(table)
        try {
          val canvas = new PDPageContentStream(doc, page)
          try {
            var mcid = 0
            var y    = 700
            // Simple 2-row loop (1 Header, 1 Data)
            for r <- 0 until 20 do {
              val row = new PDStructureElement(StandardStructureTypes.TR, table)
              table.appendKid(row)
              val cellTag =
                if r == 0 then StandardStructureTypes.TH
                else StandardStructureTypes.TD
              val text =
                if r == 0 then "Header"
                else "Data Cell"
              // Create Cell Element
              val cell = new PDStructureElement(cellTag, row)
              row.appendKid(cell)
              // --- DRAW CONTENT WITH TAG ---
              val dictionary = new COSDictionary
              dictionary.setInt(COSName.MCID, mcid)
              canvas.beginMarkedContent(COSName.P, PDPropertyList.create(dictionary))
              canvas.beginText()
              canvas.setFont(PDType1Font(FontName.HELVETICA_BOLD), 12)
              canvas.newLineAtOffset(50, y)
              canvas.showText(text)
              canvas.endText()
              canvas.endMarkedContent()
              // Link the structure element to the MCID on this page
              val mcr = new PDMarkedContentReference
              mcr.setPage(page)
              mcr.setMCID(mcid)
              cell.appendKid(mcr)
              y -= 20 // Move to next row

              mcid += 1
            }
          } finally if canvas != null then canvas.close()
        }
        // 3. Finalize: Set the page's parent tree index (simplified)
        page.getCOSObject.setInt(COSName.STRUCT_PARENTS, 0)

        doc
      }
    }

    taggedTableExample()
  }

  def testRawOdfBoxAttempt2(): PDDocument = {
    val document = new PDDocument
    val page     = new PDPage
    document.addPage(page)
    // 1. Setup the basic structure tree// 1. Setup the basic structure tree

    val structureTreeRoot = new PDStructureTreeRoot
    document.getDocumentCatalog.setStructureTreeRoot(structureTreeRoot)

    // 2. Create the Document structure element
    val root = new PDStructureElement(StandardStructureTypes.DOCUMENT, null)
    root.setTitle("Main PDF Document")
    root.setAlternateDescription("The root structure for this accessible PDF.")

    // 2. Add a Paragraph Element
    val p = new PDStructureElement(StandardStructureTypes.P, root)
    root.appendKid(p)
    p.setTitle("this is a paragraph")

    // 3. Append the Document element to the tree root
    structureTreeRoot.appendKid(root)

    ////

    var mcid     = 0 // Increment this for every new tagged element on the page
    val cellText = "Row 1, Col 1"
    // 1. Create the properties dictionary for the marked content
    val dictionary    = new COSDictionary
    val contentStream = new PDPageContentStream(document, page)

    dictionary.setInt(COSName.MCID, mcid)

    // 2. Start the Marked Content Sequence
    // Use COSName.P for general text or a custom name like "TD" for table cells
    contentStream.beginMarkedContent(COSName.P, PDPropertyList.create(dictionary))

    // 3. Draw your text as usual
    contentStream.beginText()
    contentStream.setFont(new PDType1Font(FontName.TIMES_ROMAN), 12)
    contentStream.newLineAtOffset(50, 700) // Set position

    contentStream.showText(cellText)
    contentStream.endText()

    // 4. End the Marked Content Sequence
    contentStream.endMarkedContent()

    contentStream.close()

    document
  }

  // based off of https://www.tutorialspoint.com/pdfbox/pdfbox_document_properties.htm
  def testRawOdfBoxAttempt1(): PDDocument = {
    val document = new PDDocument
    val page     = new PDPage
    document.addPage(page)
    document.setMetaData(date = new GregorianCalendar)

    var mcid       = 1
    val dictionary = new COSDictionary
    dictionary.setInt(COSName.MCID, mcid)
    val contentStream = new PDPageContentStream(document, page)

    contentStream.beginMarkedContent(COSName.A, PDPropertyList.create(dictionary))
    contentStream.beginText()
    contentStream.setFont(new PDType1Font(FontName.TIMES_ROMAN), 12)
    contentStream.newLineAtOffset(25, 500)
    contentStream.setLeading(14.5f)
    val text1 = "This is the sample line 1"
    contentStream.showText(text1)
    val text2 = "This is the sample line2"
    contentStream.newLine()
    contentStream.showText(text2)
    contentStream.endMarkedContent()

    contentStream.endText()

    contentStream.close()

    document
  }
}

object TestPdfController {
  val logger = Logger(TestPdfController.getClass)

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

  // <html lang="en-GB"> changes the pdf language meta data, otherwise it's EN-US by default
  def testHtml: String =
    """
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
       |  width: 100%;
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
       |</style>
       |</head>
       |<body>
       |<div class="logo-container">
       |  <img class="logo" src="gov-uk-logo.png" alt="GOV.UK"/>
       |  <div class="logo-text">Senior Accounting Officer notification and certificate</div>
       |</div>
       |<bookmarks>
       | <bookmark name="This is a Heading" href="#heading"/>
       |</bookmarks>
       |
       |<h1 id="heading">This is a Heading</h1>
       |<p>This is a paragraph.</p>
       | <table>
       |  <caption>Notification Details</caption>
       |  <thead>
       |    <tr><th colspan="3"><h2>Submissions<span class="continued"></span></h2></th></tr>
       |    <tr><th>Company</th>
       |    <th>Contact</th>
       |    <th>Country</th></tr>
       |  </thead>""".stripMargin
      + (0 until 5000)
        .map(index => s"""
                     |<tbody>
                     |  <tr>
                     |    <td rowspan="2">Alfreds Futterkiste ${index * 2 + 1}</td>
                     |    <td>Maria Anders</td>
                     |    <td rowspan="2">Germany</td>
                     |  </tr>
                     |  <tr>
                     |    <td>Roland Mendel</td>
                     |  </tr>
                     |</tbody>
                     |<tbody>
                     |  <tr>
                     |    <td>Centro comercial Moctezuma ${index * 2 + 2}</td>
                     |    <td>Francisco Chang</td>
                     |    <td>Mexico</td>
                     |  </tr>
                     |</tbody>
                     |""".stripMargin)
        .mkString("\n")
      + """
        |</table>
        |
        |</body>
        |</html>
        |""".stripMargin
}
