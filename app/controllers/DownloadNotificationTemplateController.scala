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

package controllers

import config.AppConfig
import controllers.actions.IdentifierAction
import org.apache.pekko.stream.scaladsl.*
import org.apache.pekko.util.ByteString
import org.apache.poi.ss.usermodel.{CellType, DataValidationHelper}
import org.apache.poi.ss.util.CellRangeAddressList
import org.apache.poi.xssf.usermodel.{XSSFSheet, XSSFWorkbook}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, FileInputStream}
import java.nio.file.{Path, Paths}
import javax.inject.Inject
import scala.jdk.CollectionConverters.*
import scala.concurrent.ExecutionContext
import DownloadNotificationTemplateController.*

import scala.collection.mutable.ListBuffer

class DownloadNotificationTemplateController @Inject (appConfig: AppConfig)(
    identify: IdentifierAction,
    mcc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def downloadFile(): Action[AnyContent] = identify { implicit request =>
    {
      val templateFilepath: Path = Paths.get(appConfig.templateFile)
      val templateFile           = templateFilepath.toFile
      if templateFile.exists && !templateFile.isDirectory then {
        val filename              = templateFilepath.getFileName
        val list: ListBuffer[Row] = ListBuffer[Row]()
        val testData: Seq[Row]    = (1 to 500)
          .foldLeft(list)((l, ?) =>
            list.addAll(
              Seq(
                Row(
                  companyName = "Test company",
                  utr = "0123456789",
                  crn = "1234567890",
                  companyType = "PLC",
                  status = "Active",
                  financialYearEndDate = "31/12/2025",
                  certificateType = "Unqualified"
                ),
                Row(
                  companyName = "Test company 2",
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
              )
            )
          )
          .toSeq

        val workbook = setData(readExcel(templateFile), testData)

        val dataContent: Source[ByteString, ?] = StreamConverters.fromInputStream(() => {
          // TODO is there a better way to do this
          val bos = new ByteArrayOutputStream
          workbook.write(bos)
          val barray = bos.toByteArray
          new ByteArrayInputStream(barray)
        })
        Ok.chunked(dataContent, inline = false, fileName = Some(filename.toString))
      } else {
        InternalServerError("Unable to provide template")
      }
    }
  }

  def readExcel(file: File): XSSFWorkbook = {
    val inputStream = FileInputStream(file)
    val workbook    = new XSSFWorkbook(inputStream)
    workbook
  }

  def setData(workbook: XSSFWorkbook, data: Seq[Row]): XSSFWorkbook = {
    val sheet          = workbook.getSheetAt(0)
    val formattedRow   = sheet.getRow(firstRowIndex)
    val formattedRange = 0 to 17

    val cellFormats = formattedRange.map { index =>
      val cell      = Option(formattedRow.getCell(index)).getOrElse(formattedRow.createCell(index))
      val cellType  = cell.getCellType
      val cellStyle = cell.getCellStyle

      cellType match {
        case CellType.FORMULA =>
          (cellStyle, Some(cell.getCellFormula), cellType)
        case _ =>
          (cellStyle, None, cellType)
      }
    }

    data.zipWithIndex.foreach((rowData, index) => {
      val row = {
        val targetIndex = firstRowIndex + index
        Option(sheet.getRow(targetIndex)).getOrElse(sheet.createRow(targetIndex))
      }
      formattedRange.foreach(index =>
        val cell = Option(row.getCell(index)).getOrElse(row.createCell(index))
        cell.setCellStyle(cellFormats(index)._1)
        cellFormats(index)._2.map(cell.setCellFormula)
        cell.setCellType(cellFormats(index)._3)
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

    Logger(this.getClass).logger.error("validations" + sheet.getDataValidations.size())
    Logger(this.getClass).logger.error(
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

    sheet.addDropDown(
      options = Array("LTD", "PLC"),
      column = Column.CompanyType,
      totalRows = data.size
    )

    sheet.addDropDown(
      options = Array("Active", "Dormant", "Administration", "Liquidation"),
      column = Column.Status,
      totalRows = data.size
    )

    sheet.addDropDown(
      options = Array("Qualified", "Unqualified"),
      column = Column.CertificateType,
      totalRows = data.size
    )

    Logger(this.getClass).logger.error(
      "validation ranges " + sheet.getDataValidations
        .iterator()
        .asScala
        .toList
        .map(_.getRegions.getCellRangeAddresses.map(_.formatAsString()).mkString)
    )

    workbook
  }
}

object DownloadNotificationTemplateController {
  val firstRowIndex = 3

  extension (sheet: XSSFSheet) {
    def helper: DataValidationHelper = sheet.getDataValidationHelper

    def addDropDown(options: Array[String], column: Column, totalRows: Int): Unit = {
      val constraint = helper.createExplicitListConstraint(options)
      val validation = helper.createValidation(
        constraint,
        new CellRangeAddressList(firstRowIndex, firstRowIndex + totalRows - 1, column.index, column.index)
      )
      validation.setSuppressDropDownArrow(true) // required in order to turn this into a dropdown
      sheet.addValidationData(validation)
    }
  }
}

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
