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

package controllers.testonly

import controllers.Execution.trampoline
import controllers.testonly.TestPoiController.*
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.*
import org.apache.pekko.util.ByteString
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddressList
import org.apache.poi.xssf.streaming.{DeferredSXSSFWorkbook, SXSSFWorkbook}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.testonly.TestPoiView

import java.io.*
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.jdk.CollectionConverters.*

class TestPoiController @Inject() (
    mcc: MessagesControllerComponents,
    testPoiView: TestPoiView
)(implicit val ec: ExecutionContext, system: ActorSystem)
    extends FrontendController(mcc)
    with I18nSupport {

  def testUiPage: Action[AnyContent] = Action { implicit request =>
    Ok(testPoiView())
  }

  def testXssf: Action[AnyContent] = downloadFile(writerType = "xssf")

  def testSxssf: Action[AnyContent] = downloadFile(writerType = "sxssf")

  def testDeferredSxssf: Action[AnyContent] = downloadFile(writerType = "deferredSxssf")

  def downloadFile(
      writerType: "xssf" | "sxssf" | "deferredSxssf",
      dataRows: Int = 1000,
      impl: Impl = Impl.C
  ): Action[AnyContent] = Action { implicit request =>
    // using .toURI to HTTP encode spaces in file names, otherwise files with spaces will not be found
    val templateFile =
      Option(getClass.getResource(templateFileConfig))
        .map(resource => new File(resource.toURI))
        .filter(file => file.exists && !file.isDirectory)
        .getOrElse(throw InternalServerException(s"Unable to provide template"))

    val dataContent = writerType match {
      case "xssf"          => xssf(templateFile, testData(dataRows), impl)
      case "sxssf"         => sxssf(templateFile, testData(dataRows), impl)
      case "deferredSxssf" => deferredSxssf(templateFile, testData(dataRows), impl)
    }

    Ok.chunked(dataContent, inline = false, fileName = Some(templateFile.getName))
  }

}

def readExcel(file: File): XSSFWorkbook = {
  val inputStream = FileInputStream(file)
  val workbook    = new XSSFWorkbook(inputStream)
  workbook
}

//XSSFWorkbook in memory only
def xssf(templateFile: File, testData: Seq[Row], implementationType: Impl)(using
    system: ActorSystem
): Source[ByteString, ?] = {

  val workbook   = readExcel(templateFile)
  val rowFormats = getDataRowFormat(workbook)

  workbook.updateDropdownConfigs(testData.size)

  workbook.setData(rowFormats, testData)

  workbook.asSource(implementationType)
}

//SXSSFWorkbook written to temp files to reduce memory usage
def sxssf(templateFile: File, data: Seq[Row], implementationType: Impl)(using
    system: ActorSystem
): Source[ByteString, ?] = {

  val xssfWorkbook = readExcel(templateFile)

  // before we create SXSSFWorkbook we need to
  // a)  create the constraints in the xssf before we create sxssf
  // b)  we need to remove all intended data rows in the XSSFWorkbook part,
  //       otherwise it'll be written to the disk and we won't be able to write to those rows anymore

  val rowFormats = getDataRowFormat(xssfWorkbook)
  val sheet      = xssfWorkbook.getSheetAt(0)
  sheet.removeRow(sheet.getRow(firstRowIndex))
  xssfWorkbook.updateDropdownConfigs(data.size)

  val sxssfWorkbook = new SXSSFWorkbook(xssfWorkbook)

  sxssfWorkbook.setData(rowFormats, data)

  sxssfWorkbook
    .asSource(implementationType)
    .watchTermination() { (_, termination) =>
      termination.foreach { _ =>
        Option(sxssfWorkbook).foreach { workbook =>
          workbook.close()
          logger.error("Stream closed, resources cleaned up.")
        }
      }
    }
}

// DeferredSXSSFWorkbook writes to fewer temp files but also reduce memory usage from XSSFWorkbook
def deferredSxssf(templateFile: File, data: Seq[Row], implementationType: Impl)(using
    system: ActorSystem
): Source[ByteString, ?] = {

  val xssfWorkbook = readExcel(templateFile)

  // before we create DeferredSXSSFWorkbook we need to
  // a)  create the constraints in the xssf before we create sxssf
  // b)  we need to remove all intended data rows in the XSSFWorkbook part,
  //       otherwise it'll already be streamed and we won't be able to write to those rows anymore
  //       If this were to happen, unlike in the SXSSFWorkbook case, currently we won't even get an exception

  val rowFormats = getDataRowFormat(xssfWorkbook)
  val sheet      = xssfWorkbook.getSheetAt(0)
  sheet.removeRow(sheet.getRow(firstRowIndex))
  xssfWorkbook.updateDropdownConfigs(data.size)

  val deferredSxssfWorkbook = new DeferredSXSSFWorkbook(xssfWorkbook)

  deferredSxssfWorkbook.configureStream(rowFormats, data)

  deferredSxssfWorkbook
    .asSource(implementationType)
    .wireTap(str => logger.error("source emit"))
    .watchTermination() { (_, termination) =>
      termination.foreach { _ =>
        Option(deferredSxssfWorkbook).foreach { workbook =>
          workbook.close()
          logger.error("Stream closed, resources cleaned up.")
        }
      }
    }
}

object TestPoiController {

  private val templateFileConfig = "/templates/testonly/Submission template- Notification and certificate (v7).xlsx"

  private[testonly] val firstRowIndex = 3

  private[testonly] val logger: slf4j.Logger = Logger(this.getClass).logger

  private def testData(rows: Int): Seq[Row] = {
    (1 to rows).map {
      case index if (index & 1) == 1 =>
        Row(
          companyName = s"Test company $index",
          utr = "0123456789",
          crn = "1234567890",
          companyType = "PLC",
          status = "Active",
          financialYearEndDate = "31/12/2025",
          certificateType = "Unqualified"
        )
      case index =>
        Row(
          companyName = s"Test company $index",
          utr = "1234567890",
          crn = "0123456789",
          companyType = "LTD",
          status = "Active",
          financialYearEndDate = "31/12/2025",
          qualified = TaxRegimes(
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
          ),
          certificateType = "Qualified",
          additionalInformation = Some("test additional info")
        )
    }
  }

  extension (sheet: Sheet) {
    def helper: DataValidationHelper = sheet.getDataValidationHelper

    def addDropdown(options: Array[String], column: Column, totalRows: Int): Unit = {
      val constraint = helper.createExplicitListConstraint(options)
      val validation = helper.createValidation(
        constraint,
        new CellRangeAddressList(firstRowIndex, firstRowIndex + totalRows - 1, column.index, column.index)
      )
      validation.setSuppressDropDownArrow(true) // required in order to turn this into a dropdown
      sheet.addValidationData(validation)
    }

    def setData(rowFormats: Seq[CellFormat], data: Seq[Row]): Unit = {
      data.zipWithIndex.foreach((rowData, index) => {
        val row = {
          val targetIndex = firstRowIndex + index
          Option(sheet.getRow(targetIndex)).getOrElse(sheet.createRow(targetIndex))
        }
        rowFormats.zipWithIndex.foreach((format, index) =>
          val cell = Option(row.getCell(index)).getOrElse(row.createCell(index))
          cell.setCellStyle(format.cellStyle)
          format.formula.map(cell.setCellFormula)
        )

        row.getCell(Column.CompanyName.index).setCellValue(rowData.companyName)
        row.getCell(Column.Utr.index).setCellValue(rowData.utr)
        row.getCell(Column.Crn.index).setCellValue(rowData.crn)
        row.getCell(Column.CompanyType.index).setCellValue(rowData.companyType)
        row.getCell(Column.Status.index).setCellValue(rowData.status)
        row.getCell(Column.FinancialYearEndDate.index).setCellValue(rowData.financialYearEndDate)
        row.getCell(Column.CorporationTax.index).setCellValue(rowData.qualified.corporationTax)
        row.getCell(Column.Vat.index).setCellValue(rowData.qualified.vat)
        row.getCell(Column.Paye.index).setCellValue(rowData.qualified.paye)
        row.getCell(Column.InsurancePremiumTax.index).setCellValue(rowData.qualified.insurancePremiumTax)
        row.getCell(Column.StampDutyLandTax.index).setCellValue(rowData.qualified.stampDutyLandTax)
        row.getCell(Column.StampDutyReserveTax.index).setCellValue(rowData.qualified.stampDutyReserveTax)
        row.getCell(Column.PetroleumRevenueTax.index).setCellValue(rowData.qualified.petroleumRevenueTax)
        row.getCell(Column.CustomsDuties.index).setCellValue(rowData.qualified.customsDuties)
        row.getCell(Column.ExciseDuties.index).setCellValue(rowData.qualified.exciseDuties)
        row.getCell(Column.BankLevy.index).setCellValue(rowData.qualified.bankLevy)
        row.getCell(Column.CertificateType.index).setCellValue(rowData.certificateType)
        rowData.additionalInformation.foreach(row.getCell(Column.AdditionalInformation.index).setCellValue)
      })

    }
  }

  enum Impl {
    case A, B, C
  }

  extension [T <: Workbook](workbook: T) {
    def asSource(implementationType: Impl)(using system: ActorSystem): Source[ByteString, ?] = {
      given blockingEc: ExecutionContext = system.dispatchers.lookup("pekko.stream.materializer.blocking-io-dispatcher")
      implementationType match {
        case Impl.A =>
          StreamConverters.fromInputStream(() => {
            val bos = new ByteArrayOutputStream
            Future {
              blocking {
                workbook match {
                  case w: DeferredSXSSFWorkbook => w.writeAvoidingTempFiles(bos)
                  case _                        => workbook.write(bos)
                }
              }
            }
            val barray = bos.toByteArray
            new ByteArrayInputStream(barray)
          })
        case Impl.B =>
          Source
            .single(workbook)
            .map(workbook => {
              val bos = new ByteArrayOutputStream()
              workbook match {
                case w: DeferredSXSSFWorkbook => w.writeAvoidingTempFiles(bos)
                case _                        => workbook.write(bos)
              }
              bos
            })
            .map(bos => ByteString(bos.toByteArray))
        case Impl.C =>
          StreamConverters
            .asOutputStream() // Creates a Source that materializes an OutputStream
            .mapMaterializedValue { outputStream =>
              Future {
                try {
                  workbook.write(outputStream)
                  outputStream.flush()
                } finally {
                  outputStream.close()
                }
              }
            }
      }

    }

    def getDataRowFormat: Seq[CellFormat] = {
      val sheet          = workbook.getSheetAt(0)
      val formattedRow   = Option(sheet.getRow(firstRowIndex)).getOrElse(sheet.createRow(firstRowIndex))
      val formattedRange = 0 to 17

      formattedRange.map { index =>
        val cell      = Option(formattedRow.getCell(index)).getOrElse(formattedRow.createCell(index))
        val cellType  = cell.getCellType
        val cellStyle = cell.getCellStyle

        cellType match {
          case CellType.FORMULA =>
            CellFormat(cellStyle, Some(cell.getCellFormula))
          case _ =>
            CellFormat(cellStyle, None)
        }
      }
    }
  }

  // data validations (dropdowns) can only be configured via XSSFWorkbook
  extension (workbook: XSSFWorkbook) {

    def updateDropdownConfigs(dataSize: Int): Unit = {
      val sheet = workbook.getSheetAt(0)

      logger.error("validations" + sheet.getDataValidations.size())
      logger.error(
        "validation ranges " + sheet.getDataValidations
          .iterator()
          .asScala
          .toList
          .map(_.getRegions.getCellRangeAddresses.map(_.formatAsString()).mkString)
      )

      // dropdowns are dataValidations attached at the sheet level and not associated to an individual cell
      // The idea here is to recreate new ones instead of than expand the ranges of existing validations (not obvious how to)

      // sheet.getDataValidations will always be a copy and therefore anything we do on them will have no actual effect
      val existingValidationList = sheet.getCTWorksheet.getDataValidations.getDataValidationList
      existingValidationList.removeAll(existingValidationList)

      // we could use sheet.getCTWorksheet.getDataValidations.getDataValidationList to update the address range of each validation via .setSqref
      // e.g. setSqref(List("D4:D5").asJava)
      // however it is not obvious how we can tell which validation is which since getSqref returns a list of any

      sheet.addDropdown(
        options = Array("LTD", "PLC"),
        column = Column.CompanyType,
        totalRows = dataSize
      )

      sheet.addDropdown(
        options = Array("Active", "Dormant", "Administration", "Liquidation"),
        column = Column.Status,
        totalRows = dataSize
      )

      sheet.addDropdown(
        options = Array("Qualified", "Unqualified"),
        column = Column.CertificateType,
        totalRows = dataSize
      )

      logger.error(
        "validation ranges " + sheet.getDataValidations
          .iterator()
          .asScala
          .toList
          .map(_.getRegions.getCellRangeAddresses.map(_.formatAsString()).mkString)
      )
    }
  }

  // set data for XSSFWorkbook & SXSSFWorkbook are identical and synchronously since they must be in memory (or in temp file)
  // before we write to outputstream
  extension (workbook: XSSFWorkbook | SXSSFWorkbook) {
    def setData(rowFormats: Seq[CellFormat], data: Seq[Row]): Unit = {
      val sheet = workbook.getSheetAt(0)
      sheet.setData(rowFormats, data)
    }
  }

  // for DeferredSXSSFWorkbook data is set using a generator function and on demand, since it is not done until we request write
  extension (workbook: DeferredSXSSFWorkbook) {
    def configureStream(rowFormats: Seq[CellFormat], data: Seq[Row]): Unit = {
      val streamingSheet = workbook.getStreamingSheetAt(0)
      streamingSheet.setRowGenerator(sheet => sheet.setData(rowFormats, data))
    }
  }

}

final case class CellFormat(cellStyle: CellStyle, formula: Option[String])

final case class TaxRegimes(
    corporationTax: Boolean = false,
    vat: Boolean = false,
    paye: Boolean = false,
    insurancePremiumTax: Boolean = false,
    stampDutyLandTax: Boolean = false,
    stampDutyReserveTax: Boolean = false,
    petroleumRevenueTax: Boolean = false,
    customsDuties: Boolean = false,
    exciseDuties: Boolean = false,
    bankLevy: Boolean = false
)

final case class Row(
    companyName: String,
    utr: String,
    crn: String,
    companyType: "PLC" | "LTD",
    status: "Active" | "Dormant" | "Administration" | "Liquidation",
    financialYearEndDate: String,
    certificateType: "Qualified" | "Unqualified",
    qualified: TaxRegimes = TaxRegimes(),
    additionalInformation: Option[String] = None
)

enum Column(val index: Int) {
  case CompanyName           extends Column(0)
  case Utr                   extends Column(1)
  case Crn                   extends Column(2)
  case CompanyType           extends Column(3)
  case Status                extends Column(4)
  case FinancialYearEndDate  extends Column(5)
  case CorporationTax        extends Column(6)
  case Vat                   extends Column(7)
  case Paye                  extends Column(8)
  case InsurancePremiumTax   extends Column(9)
  case StampDutyLandTax      extends Column(10)
  case StampDutyReserveTax   extends Column(11)
  case PetroleumRevenueTax   extends Column(12)
  case CustomsDuties         extends Column(13)
  case ExciseDuties          extends Column(14)
  case BankLevy              extends Column(15)
  case CertificateType       extends Column(16)
  case AdditionalInformation extends Column(17)
}
