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
import controllers.testonly.TestPdfController.*
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.{Source, StreamConverters}
import org.apache.pekko.util.ByteString
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.text
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.testonly.*

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.util.Try

import java.io.*
import javax.inject.Inject

class TestPdfController @Inject() (
    mcc: MessagesControllerComponents,
    openHtmlToPdfService: OpenHtmlToPdfService,
    view: OpenHtmlToPdfView,
    notificationPdfTemplate: NotificationPdfView,
    certificatePdfTemplate: CertificatePdfView,
    signUpPdfTemplate: SignUpPdfView
)(implicit val ec: ExecutionContext, actorSystem: ActorSystem)
    extends FrontendController(mcc)
    with I18nSupport {

  def testSignUpPdf(): Action[AnyContent] = Action { implicit request =>
    val html    = signUpPdfTemplate(testSignUpData()).toString
    val content = openHtmlToPdfService.builderFor(html).asSource
    Ok.chunked(
      content,
      inline = false,
      fileName = Some("test-sign-up.pdf")
    )
  }

  def testNotificationPdf(rows: Int): Action[AnyContent] = Action { implicit request =>
    val html    = notificationPdfTemplate(testNotificationData(rows)).toString
    val content = openHtmlToPdfService.builderFor(html).asSource
    Ok.chunked(
      content,
      inline = false,
      fileName = Some("test-notification.pdf")
    )
  }

  def testCertificatePdf(rows: Int): Action[AnyContent] = Action { implicit request =>
    val html    = certificatePdfTemplate(testCertificateData(rows)).toString
    val content = openHtmlToPdfService.builderFor(html).asSource
    Ok.chunked(
      content,
      inline = false,
      fileName = Some("test-certificate.pdf")
    )
  }

  def onSubmit(): Action[AnyContent] = Action { implicit request =>
    emptyForm()
      .bindFromRequest()
      .fold(
        _ => Ok(view(emptyForm())),
        html => {
          val builder = openHtmlToPdfService.builderFor(html)

          val pdfBuffer = ByteArrayOutputStream()
          builder.toStream(pdfBuffer)

          Try(builder.run()).toEither.fold(
            throwable => {
              val stringWriter = new StringWriter
              val printWriter  = new PrintWriter(stringWriter)
              throwable.printStackTrace(printWriter)

              val stackTraceAsString = stringWriter.getBuffer
              val contentBuffer      = {
                StringBuffer()
                  .append("---------------------- [Error] --------------------------\n")
                  .append(stackTraceAsString)
                  .append("\n---------------------- [End Error] --------------------------\n")
                  .append("\n---------------------- [HTML] --------------------------\n")
                  .append(html)
                  .append("\n---------------------- [End HTML] --------------------------\n")
              }
              Try(pdfBuffer.close())
              Try(stringWriter.close())
              Try(printWriter.close())
              val errStream = new ByteArrayInputStream(contentBuffer.toString.getBytes)
              Ok.chunked(
                StreamConverters.fromInputStream(() => errStream),
                inline = false,
                fileName = Some("error.txt")
              )
            },
            _ => {
              val pdfBytes = pdfBuffer.toByteArray

              Ok.chunked(
                StreamConverters.fromInputStream(() => ByteArrayInputStream(pdfBytes)),
                inline = false,
                fileName = Some("test.pdf")
              )
            }
          )
        }
      )
  }

  def openHtml(): Action[AnyContent] = Action { implicit request =>
    Ok(view(emptyForm()))
  }

  def example(rows: Int): Action[AnyContent] = Action { implicit request =>
    Ok(notificationPdfTemplate(OpenHtmlToPdfService.testNotificationData(rows)).toString())
  }

}

object TestPdfController {

  final case class Contact(name: String, email: String)

  final case class SignUp(
      companyName: String,
      crn: String,
      utr: String,
      subscriptionDate: String,
      subscriptionId: String,
      contacts: Seq[Contact]
  )

  final case class Certificate(
      saoName: String,
      saoEmail: String,
      submitterName: String,
      submissionDate: String,
      submissionId: String,
      companies: Seq[Certificate.Row],
      additionalInformation: Option[String] = None
  )

  object Certificate {
    final case class Row(
        companyName: String,
        utr: String,
        crn: String,
        companyType: "PLC" | "LTD",
        status: "Active" | "Dormant" | "Administration" | "Liquidation",
        financialYearEndDate: String,
        qualifiedRegimes: TaxRegimes = TaxRegimes(),
        additionalInformation: Option[String] = None
    )
    object Row {
      extension (row: Row) {
        def qualifiedRegimesAsText: String = {
          val regimes = row.qualifiedRegimes
          val builder = ListBuffer[String]()

          if regimes.corporationTax then builder.append("Corporation Tax")
          if regimes.vat then builder.append("VAT")
          if regimes.paye then builder.append("PAYE")
          if regimes.insurancePremiumTax then builder.append("Insurance Premium Tax")
          if regimes.stampDutyLandTax then builder.append("Stamp Duty Land Tax")
          if regimes.stampDutyReserveTax then builder.append("Stamp Duty Reserve Tax")
          if regimes.petroleumRevenueTax then builder.append("Petroleum Revenue Tax")
          if regimes.customsDuties then builder.append("Customs Duties")
          if regimes.exciseDuties then builder.append("Excise Duties")
          if regimes.bankLevy then builder.append("Bank Levy")
          builder.mkString(", ")
        }

        def toNotificationRow(
            index: Int
        ): Notification.Row = Notification.Row(
          row.companyName.replace("$index", index.toString),
          row.utr,
          row.crn,
          row.companyType,
          row.status,
          row.financialYearEndDate
        )
      }
    }

    extension (cert: Certificate) {
      def qualified: Seq[Certificate.Row] =
        cert.companies.filter(_.qualifiedRegimes.isQualified)

      def unqualified: Seq[Certificate.Row] =
        cert.companies.filterNot(_.qualifiedRegimes.isQualified)
    }
  }

  final case class SaoTenure(name: String, startDate: Option[String] = None, endDate: Option[String] = None)

  final case class Notification(
      companyName: String,
      financialYearEndDate: String,
      submissionDate: String,
      submissionId: String,
      saoHistory: Seq[SaoTenure],
      companies: Seq[Notification.Row],
      additionalInformation: Option[String] = None
  )

  object Notification {
    final case class Row(
        companyName: String,
        utr: String,
        crn: String,
        companyType: "PLC" | "LTD",
        status: "Active" | "Dormant" | "Administration" | "Liquidation",
        financialYearEndDate: String
    )

    extension (row: Row) {
      def toCertificateRow(
          index: Int,
          qualifiedRegimes: TaxRegimes = TaxRegimes(),
          additionalInformation: Option[String] = None
      ): Certificate.Row = Certificate.Row(
        row.companyName.replace("$index", index.toString),
        row.utr,
        row.crn,
        row.companyType,
        row.status,
        row.financialYearEndDate,
        qualifiedRegimes,
        additionalInformation
      )
    }
  }

  private def emptyForm(): Form[String] =
    Form(
      "value" -> text()
    )

  val logger: Logger = Logger(TestPdfController.getClass)

  extension (builder: PdfRendererBuilder) {
    def asSource(using system: ActorSystem): Source[ByteString, ?] = StreamConverters
      .fromInputStream(() => {
        given blockingEc: ExecutionContext =
          system.dispatchers.lookup("pekko.stream.materializer.blocking-io-dispatcher")

        val pos = new PipedOutputStream

        // Set the output stream
        builder.toStream(pos)

        Future {
          blocking {
            // Run the conversion
            builder.run()
          }
        }.onComplete { _ =>
          pos.close()
        }
        new PipedInputStream(pos)
      })
  }

}

private[testonly] class OpenHtmlToPdfService {
  def builderFor(content: String): PdfRendererBuilder = {

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
      content,
      this.getClass.getResource("/pdf/").toURI.toString
    )

    builder
  }

}

private[testonly] object OpenHtmlToPdfService {

  def testSignUpData(): SignUp = SignUp(
    companyName = "Test ABC Limited",
    crn = "SC123456",
    utr = "5928374610",
    subscriptionDate = "12 January 2025",
    subscriptionId = "XMPLR0123456789",
    contacts = List(
      Contact(name = "Fake Ethan Easton", email = "eeaston@test.co.uk"),
      Contact(name = "Fake Amanda Hawthorne", email = "ahawthorne@test.co.uk")
    )
  )

  private val testCompanySeeds: Seq[Certificate.Row] = Seq(
    Certificate.Row(
      companyName =
        "TEST NAME OF THE COMPANY WITH THE LONGEST NAME SO FAR INCORPORATED AT THE REGISTRY OF COMPANIES IN ENGLAND AND WALES AND ENCOMPASSING THE REGISTRIES BASED IN SCOTLAND $index",
      utr = "0123456789",
      crn = "9876543210",
      companyType = "LTD",
      status = "Administration",
      financialYearEndDate = "31 Jan 2025",
      qualifiedRegimes = TaxRegimes(
        corporationTax = true,
        vat = true,
        paye = true,
        insurancePremiumTax = true,
        stampDutyLandTax = true,
        stampDutyReserveTax = true,
        petroleumRevenueTax = true,
        customsDuties = true,
        exciseDuties = true,
        bankLevy = true
      )
    ),
    Certificate.Row(
      companyName = "Test Company $index",
      utr = "0123456789",
      crn = "9876543210",
      companyType = "PLC",
      status = "Active",
      financialYearEndDate = "31 Jan 2025",
      qualifiedRegimes = TaxRegimes(
        corporationTax = true,
        vat = true,
        paye = true
      )
    ),
    Certificate.Row(
      companyName = "Test Halcyon Merchants International $index",
      utr = "BR904583",
      crn = "4720938165",
      companyType = "PLC",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Pinnacle Freight and Forwarding Solutions $index",
      utr = "BR229831",
      crn = "6192837465",
      companyType = "PLC",
      status = "Administration",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Arkwright and Co $index",
      utr = "BR796541",
      crn = "4082931756",
      companyType = "PLC",
      status = "Active",
      financialYearEndDate = "31 Mar 2025",
      qualifiedRegimes = TaxRegimes(
        vat = true
      )
    ),
    Certificate.Row(
      companyName = "Test Vortex Supply Co $index",
      utr = "BR112045",
      crn = "3847291056",
      companyType = "LTD",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Nexora Trading $index",
      utr = "BR348562",
      crn = "7384920156",
      companyType = "LTD",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Caldwell Imports and Distribution Partners $index",
      utr = "BR457294",
      crn = "2938471605",
      companyType = "PLC",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Stratos Ventures $index",
      utr = "BR561038",
      crn = "8473920165",
      companyType = "LTD",
      status = "Dormant",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Ironclad Exports $index",
      utr = "BR674820",
      crn = "5039284716",
      companyType = "LTD",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Luminary Goods and Global Trade Services $index",
      utr = "BR783451",
      crn = "1629384750",
      companyType = "LTD",
      status = "Administration",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Tesseract Cargo $index",
      utr = "BR891267",
      crn = "9283746150",
      companyType = "LTD",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Drift and Sons $index",
      utr = "BR017392",
      crn = "3849201756",
      companyType = "LTD",
      status = "Dormant",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Orizon Distributers $index",
      utr = "BR132047",
      crn = "6038291475",
      companyType = "PLC",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Meridian Haulers International Freight $index",
      utr = "BR245718",
      crn = "7192038456",
      companyType = "LTD",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Cobalt Solutions $index",
      utr = "BR359204",
      crn = "8203947165",
      companyType = "LTD",
      status = "Administration",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Farpoint Trading $index",
      utr = "BR463851",
      crn = "5739204816",
      companyType = "LTD",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Verity Logistics and Supply Chain Management $index",
      utr = "BR578430",
      crn = "2048371965",
      companyType = "PLC",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Quantum Carriers $index",
      utr = "BR682094",
      crn = "9371840256",
      companyType = "LTD",
      status = "Dormant",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Solace Freight $index",
      utr = "BR803267",
      crn = "6394817025",
      companyType = "LTD",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Templar Supplies and Procurement Solutions $index",
      utr = "BR917845",
      crn = "3748021965",
      companyType = "LTD",
      status = "Liquidation",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Echelon Brokers $index",
      utr = "BR024673",
      crn = "8102937456",
      companyType = "LTD",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Silverline Cargo $index",
      utr = "BR138290",
      crn = "5920384716",
      companyType = "LTD",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Wavecrest Imports $index",
      utr = "BR241067",
      crn = "7038291645",
      companyType = "LTD",
      status = "Dormant",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Crestview Partners and Associated Trading $index",
      utr = "BR356894",
      crn = "2947183056",
      companyType = "PLC",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Novaline Exports $index",
      utr = "BR469520",
      crn = "9083274165",
      companyType = "LTD",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Tangent Wholesale $index",
      utr = "BR573148",
      crn = "4817392056",
      companyType = "LTD",
      status = "Liquidation",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Fieldstone Commerce and Overseas Distribution $index",
      utr = "BR687035",
      crn = "6293018475",
      companyType = "LTD",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Auris Distribution $index",
      utr = "BR791862",
      crn = "3058492716",
      companyType = "LTD",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Stellarex Holdings $index",
      utr = "BR804729",
      crn = "7194830265",
      companyType = "LTD",
      status = "Dormant",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Ravenport Traders and International Brokers $index",
      utr = "BR918345",
      crn = "5382910746",
      companyType = "LTD",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    ),
    Certificate.Row(
      companyName = "Test Ironveil Ventures $index",
      utr = "BR025671",
      crn = "8047293165",
      companyType = "PLC",
      status = "Active",
      financialYearEndDate = "31 Mar 2025"
    )
  )

  private def genSeq[A](total: Int, generatorFunction: Int => A): Seq[A] = {
    val lb = ListBuffer[A]()
    @tailrec
    def loop(total: Int, counter: Int = 1): Unit = {
      if counter <= total then
        lb.append(generatorFunction(counter))
        loop(total, counter + 1)
      else ()
    }
    loop(total)
    lb.toSeq
  }

  def genNotificationTestCompanies(total: Int): Seq[Notification.Row] = {
    def getTestCompany(index: Int): Notification.Row = {
      val base = testCompanySeeds(index % testCompanySeeds.length)
      base.toNotificationRow(index)
    }
    genSeq(total, getTestCompany)
  }

  def genCertificateTestCompanies(
      total: Int,
      additionalInformation: Option[String] = None
  ): Seq[Certificate.Row] = {
    def getTestCompany(index: Int): Certificate.Row = {
      val base = testCompanySeeds(index % testCompanySeeds.length)
      base.copy(
        companyName = base.companyName.replace("$index", index.toString),
        qualifiedRegimes =
          if index < 100 then base.qualifiedRegimes else TaxRegimes(),
        additionalInformation = additionalInformation
      )
    }
    genSeq(total, getTestCompany)
  }

  def testNotificationData(rows: Int)(using Materializer, ExecutionContext): Notification = {
    val additionalInformation = ""
    Notification(
      companyName = "Test ABC Limited",
      financialYearEndDate = "21 December 2024",
      submissionDate = "12 May 2025",
      submissionId = "XMPLR0123456789",
      saoHistory = List(
        SaoTenure(name = "Fake Jackson Brown", startDate = Some("01 June 2024")),
        SaoTenure(name = "Fake Ashley Ross", startDate = Some("01 January 2024"), endDate = Some("31 May 2024"))
      ),
      companies = genNotificationTestCompanies(rows),
      additionalInformation = Some(additionalInformation)
    )
  }

  def testCertificateData(rows: Int)(using Materializer, ExecutionContext): Certificate = {
    val additionalInformation = ""
    Certificate(
      saoName = "Test Jackson Brown",
      saoEmail = "jbrown@test.co.uk",
      submitterName = "Jake Allen",
      submissionDate = "12 May 2025",
      submissionId = "XMPLR0123456789",
      companies = genCertificateTestCompanies(rows, Some(additionalInformation)),
      additionalInformation = Some(additionalInformation)
    )
  }

}

object LorumIpsum {

  // LorumIpsum block pulled from wikipedia: http://en.wikipedia.org/wiki/Lorem_ipsum
  val lorumIpsumBlock =
    "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n"

  def generate(totalBytes: Long): String = {
    val byteArray       = lorumIpsumBlock.getBytes("utf-8")
    val numberOfRepeats = totalBytes / byteArray.length
    val offset          = byteArray.slice(0, (totalBytes % byteArray.length).toInt)
    val sb              = StringBuilder()

    @tailrec
    def loop(total: Long, counter: Long = 0): Unit = {
      if counter < total then
        sb.append(lorumIpsumBlock)
        loop(total, counter + 1)
      else ()
    }
    loop(1)

    sb.append(String(offset))
    sb.mkString
  }

}
