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
import org.apache.pekko.stream.IOResult
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
    openHtmlToPdfService: OpenHtmlToPdfService,
    apacheFopService: ApacheFopService
)(implicit val ec: ExecutionContext, system: ActorSystem)
    extends FrontendController(mcc)
    with I18nSupport {

  def testPdf(): Action[AnyContent] = Action { implicit request =>
//    val document = openHtmlToPdfService.testPdf()
//    val dataContent = document.asSource

    val dataContent = apacheFopService.testPdf()

    Ok.chunked(dataContent, inline = false, fileName = Some("test.pdf"))
  }

}

class ApacheFopService @Inject() (implicit val ec: ExecutionContext, system: ActorSystem) {

  import org.apache.fop.apps.*
  import javax.xml.transform.{Source as _, *}
  import javax.xml.transform.stream.StreamSource
  import javax.xml.transform.sax.SAXResult
  import org.apache.xmlgraphics.util.MimeConstants
  import java.io.StringReader

  private val userAgentBlock: FOUserAgent => Unit = { foUserAgent =>
    foUserAgent.setAccessibility(true)
    foUserAgent.setPdfUAEnabled(true)
    foUserAgent.setAuthor("HMRC forms service")
    foUserAgent.setProducer("HMRC forms services")
    foUserAgent.setCreator("HMRC forms services")
    foUserAgent.setSubject("SAO Notification Confirmation")
    foUserAgent.setTitle("SAO Notification Confirmation")
  }

  private def xslTransformations: String = {
    s"""<?xml version="1.0" encoding="UTF-8"?>
      |<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      |                xmlns:fo="http://www.w3.org/1999/XSL/Format">
      |
      |    <!-- Root template -->
      |    <xsl:template match="/">
      |        <fo:root 
      |          xmlns:fo="http://www.w3.org/1999/XSL/Format" 
      |          xmlns:fox="http://xmlgraphics.apache.org/fop/extensions"
      |          xml:lang="en-GB"
      |          font-family="Helvetica, sans-serif"
      |        >
      |            <fo:layout-master-set>
      |                <fo:simple-page-master
      |                  master-name="A4"
      |                  page-height="29.7cm"
      |                  page-width="21cm"
      |                  margin-top="1cm"
      |                  margin-bottom="1cm"
      |                  margin-left="2cm"
      |                  margin-right="2cm"
      |                >
      |                  <fo:region-body margin-top="3cm" margin-bottom="2cm"/>
      |                  <fo:region-before extent="3cm"/>
      |                </fo:simple-page-master>
      |            </fo:layout-master-set>
      |            
      |            <fo:bookmark-tree>
      |               <fo:bookmark internal-destination="h1_1">
      |                 <fo:bookmark-title>This is a Heading</fo:bookmark-title>
      |               </fo:bookmark>
      |               <fo:bookmark internal-destination="h1_2">
      |                 <fo:bookmark-title>Appendix A: Tables</fo:bookmark-title>
      |               </fo:bookmark>
      |            </fo:bookmark-tree>
      |                
      |            <fo:page-sequence master-reference="A4">
      |                $header
      |                <fo:flow flow-name="xsl-region-body">
      |                    <fo:block role="H1" font-size="24pt" font-weight="bold" margin-bottom="20pt"  id="h1_1" >This is a Heading</fo:block>
      |                    <fo:block role="P" font-size="16pt" margin-bottom="8pt">This is a paragraph</fo:block>
      |                   
      |                    <fo:block role="H1" font-size="24pt" font-weight="bold" id="h1_2" page-break-before="always" margin-bottom="20pt">Appendix A: Tables</fo:block>
      |                    $booklist
      |
      |                    $table
      |                </fo:flow>
      |            </fo:page-sequence>
      |        </fo:root>
      |        
      |
      |    </xsl:template>
      |</xsl:stylesheet>
      |""".stripMargin
  }

  def header: String =
    s"""
      |   <fo:static-content flow-name="xsl-region-before" role="Artifact" >
      |        <fo:block role="Artifact">
      |           <fo:external-graphic
      |             role="Artifact"
      |             content-width="148px" content-height="20px"
      |             src="url(${getClass.getResource("/fop/gov-uk-logo.png").toURI.toString})"
      |             padding-right="1cm"
      |           />
      |           <fo:block role="Artifact" font-size="16pt" font-weight="bold" margin-bottom="1mm">SAO Notification Confirmation</fo:block>
      |         </fo:block>
      |    </fo:static-content>
      |""".stripMargin

  def booklist: String =
    """
      |                    <fo:block role="H2" font-size="24pt" font-weight="bold" margin-bottom="20pt">List of Books</fo:block>
      |                    <fo:block role="H3" font-size="18pt" margin-bottom="12pt">Books:</fo:block>
      |
      |                    <!-- Loop through each book and format its details -->
      |                    <xsl:for-each select="books/book">
      |                        <fo:block role="P" font-size="16pt" margin-bottom="8pt">
      |                            <xsl:value-of select="title"/> by <xsl:value-of select="author"/> (Year: <xsl:value-of select="year"/>)
      |                        </fo:block>
      |                    </xsl:for-each>
      |""".stripMargin

  def table: String =
    """<fo:table width="100%" table-layout="fixed">
      |  <fo:table-column xmlns:fox="http://xmlgraphics.apache.org/fop/extensions" fox:header="true" column-width="proportional-column-width(1)"/>
      |  <fo:table-column column-width="proportional-column-width(1)"/>
      |  <fo:table-column column-width="proportional-column-width(1)"/>
      |  <fo:table-header font-weight="bold">
      |    <fo:table-row>
      |      <fo:table-cell border="1pt solid black" padding-left="1pt">
      |        <fo:block>Header Scope = Both</fo:block>
      |      </fo:table-cell>
      |      <fo:table-cell border="1pt solid black" padding-left="1pt">
      |        <fo:block>Header Scope = Column</fo:block>
      |      </fo:table-cell>
      |      <fo:table-cell border="1pt solid black" padding-left="1pt">
      |        <fo:block>Header Scope = Column</fo:block>
      |      </fo:table-cell>
      |    </fo:table-row>
      |  </fo:table-header>
      |  <fo:table-body>
      |    <fo:table-row>
      |      <fo:table-cell border="1pt solid black" padding-left="1pt" font-weight="bold">
      |        <fo:block>Header Scope = Row</fo:block>
      |      </fo:table-cell>
      |      <fo:table-cell border="1pt solid black" padding-left="1pt">
      |        <fo:block>Cell 1.1</fo:block>
      |      </fo:table-cell>
      |      <fo:table-cell border="1pt solid black" padding-left="1pt">
      |        <fo:block>Cell 1.2</fo:block>
      |      </fo:table-cell>
      |    </fo:table-row>
      |    <fo:table-row>
      |      <fo:table-cell border="1pt solid black" padding-left="1pt" font-weight="bold">
      |        <fo:block>Header Scope = Row</fo:block>
      |      </fo:table-cell>
      |      <fo:table-cell border="1pt solid black" padding-left="1pt">
      |        <fo:block>Cell 2.1</fo:block>
      |      </fo:table-cell>
      |      <fo:table-cell border="1pt solid black" padding-left="1pt">
      |        <fo:block>Cell 2.2</fo:block>
      |      </fo:table-cell>
      |    </fo:table-row>
      |    <fo:table-row>
      |      <fo:table-cell border="1pt solid black" padding-left="1pt" role="TD">
      |        <fo:block>Non-header</fo:block>
      |      </fo:table-cell>
      |      <fo:table-cell border="1pt solid black" padding-left="1pt">
      |        <fo:block>Cell 3.1</fo:block>
      |      </fo:table-cell>
      |      <fo:table-cell border="1pt solid black" padding-left="1pt">
      |        <fo:block>Cell 3.2</fo:block>
      |      </fo:table-cell>
      |    </fo:table-row>
      |  </fo:table-body>
      |</fo:table>""".stripMargin

  private def inputXml: String =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<books>
      |    <book>
      |        <title>Spring Boot in Action</title>
      |        <author>Craig Walls</author>
      |        <year>2022</year>
      |    </book>
      |    <book>
      |        <title>Java: The Complete Reference</title>
      |        <author>Herbert Schildt</author>
      |        <year>2023</year>
      |    </book>
      |</books>
      |""".stripMargin

  def testPdf(): Source[ByteString, Future[IOResult]] = {
    val fopFactory  = FopFactory.newInstance(new File(getClass.getResource("/fop/fop.xconf").toURI))
    val foUserAgent = fopFactory.newFOUserAgent()
    userAgentBlock(foUserAgent)

    val pos   = new PipedOutputStream
    def fop() = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pos)

    val factory     = TransformerFactory.newInstance
    val transformer = factory.newTransformer(new StreamSource(new StringReader(xslTransformations)))
    val src         = new StreamSource(new StringReader(inputXml))

    StreamConverters.fromInputStream(() => {
      val inputStream                    = new PipedInputStream(pos)
      given blockingEc: ExecutionContext =
        system.dispatchers.lookup("pekko.stream.materializer.blocking-io-dispatcher")

      Future {
        blocking {
          val res = new SAXResult(fop().getDefaultHandler)
          transformer.transform(src, res)
        }
      }.recoverWith { e =>
        TestPdfController.logger.error("FOP error", e)
        throw e
      }.onComplete { _ =>
        pos.close()
      }

      inputStream
    })
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
       |  ${data.foldLeft("")((concat, row) => s"""
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
          |""".stripMargin)}
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
