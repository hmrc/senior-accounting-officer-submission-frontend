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

package services

import com.univocity.parsers.csv.{CsvParser, CsvParserSettings}
import models.upload.TemplateParseResult.{Invalid, Valid}
import models.upload.*

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.*
import scala.util.control.NonFatal

import java.io.StringReader
import javax.inject.Inject

class UploadTemplateCsvParser @Inject() {

  import UploadTemplateCsvParser.*

  def parse(csv: String): TemplateParseResult = {
    val errors = ArrayBuffer.empty[TemplateParseError]

    val rows =
      try parseCsvRows(csv)
      catch {
        case NonFatal(e) =>
          return Invalid(
            Seq(
              TemplateParseError(
                line = 0,
                column = None,
                code = "invalid_csv",
                message = s"Unable to parse CSV content: ${e.getMessage}"
              )
            )
          )
      }

    validateSectionRow(rows.lift(SectionRowIndex), errors)
    validateHeaderRow(rows.lift(HeaderRowIndex), errors)

    if errors.nonEmpty then Invalid(errors.toSeq)
    else parseDataRows(rows, errors)
  }

  private def parseCsvRows(csv: String): Seq[Array[String]] = {
    val settings = CsvParserSettings()
    settings.setLineSeparatorDetectionEnabled(true)
    settings.setReadInputOnSeparateThread(false)
    settings.setNullValue("")
    settings.setEmptyValue("")
    settings.setMaxColumns(64)
    settings.setMaxCharsPerColumn(100000)

    val parser    = CsvParser(settings)
    val sanitized = csv.stripPrefix("\uFEFF")

    parser.parseAll(StringReader(sanitized)).asScala.toSeq
  }

  private def validateSectionRow(rowOpt: Option[Array[String]], errors: ArrayBuffer[TemplateParseError]): Unit =
    rowOpt.fold {
      errors += TemplateParseError(
        line = SectionLineNumber,
        column = None,
        code = "missing_section_row",
        message = s"Line $SectionLineNumber is missing section headers."
      )
    } { row =>
      val notificationSection = cellValue(row, NotificationSectionIndex)
      val certificateSection  = cellValue(row, CertificateSectionIndex)

      if notificationSection != NotificationSectionExpected then
        errors += TemplateParseError(
          line = SectionLineNumber,
          column = Some("Notification"),
          code = "invalid_section_row",
          message =
            s"Line $SectionLineNumber must contain '$NotificationSectionExpected' in column B."
        )

      if certificateSection != CertificateSectionExpected then
        errors += TemplateParseError(
          line = SectionLineNumber,
          column = Some("Certificate"),
          code = "invalid_section_row",
          message =
            s"Line $SectionLineNumber must contain '$CertificateSectionExpected' in column G."
        )
    }

  private def validateHeaderRow(rowOpt: Option[Array[String]], errors: ArrayBuffer[TemplateParseError]): Unit =
    rowOpt.fold {
      errors += TemplateParseError(
        line = HeaderLineNumber,
        column = None,
        code = "missing_header_row",
        message = s"Line $HeaderLineNumber is missing the template table headers."
      )
    } { row =>
      val hasExtraNonEmptyColumns = row.drop(ExpectedHeaders.length).exists(_.trim.nonEmpty)

      if hasExtraNonEmptyColumns then
        errors += TemplateParseError(
          line = HeaderLineNumber,
          column = None,
          code = "unexpected_header_columns",
          message =
            s"Line $HeaderLineNumber contains more than ${ExpectedHeaders.length} populated columns."
        )

      ExpectedHeaders.zipWithIndex.foreach { case (expectedHeader, idx) =>
        val actualHeader = cellValue(row, idx)

        if actualHeader != expectedHeader then
          errors += TemplateParseError(
            line = HeaderLineNumber,
            column = Some(expectedHeader),
            code = "header_mismatch",
            message =
              s"Line $HeaderLineNumber column ${idx + 1} expected '$expectedHeader' but found '$actualHeader'."
          )
      }
    }

  private def parseDataRows(
      rows: Seq[Array[String]],
      errors: ArrayBuffer[TemplateParseError]
  ): TemplateParseResult = {
    val parsedRows = ArrayBuffer.empty[ParsedSubmissionRow]

    rows.zipWithIndex.drop(DataStartIndex).foreach { case (rawRow, idx) =>
      val lineNumber = idx + 1
      val row        = normalizedDataColumns(rawRow)

      val hasExtraNonEmptyColumns = rawRow.drop(ExpectedHeaders.length).exists(_.trim.nonEmpty)
      if hasExtraNonEmptyColumns then
        errors += TemplateParseError(
          line = lineNumber,
          column = None,
          code = "unexpected_data_columns",
          message = s"Line $lineNumber has values beyond column ${ExpectedHeaders.length}."
        )

      if row.exists(_.nonEmpty) then
        val rowErrorsAtStart = errors.size

        RequiredNotificationColumnIndexes.foreach { index =>
          if row(index).isEmpty then
            errors += TemplateParseError(
              line = lineNumber,
              column = Some(ExpectedHeaders(index)),
              code = "missing_required_value",
              message = s"Line $lineNumber ${ExpectedHeaders(index)} is required."
            )
        }

        validateAllowedValue(
          lineNumber = lineNumber,
          columnIndex = CompanyTypeIndex,
          value = row(CompanyTypeIndex),
          allowed = AllowedCompanyTypes,
          errors = errors,
          code = "invalid_company_type"
        )

        validateAllowedValue(
          lineNumber = lineNumber,
          columnIndex = CompanyStatusIndex,
          value = row(CompanyStatusIndex),
          allowed = AllowedCompanyStatus,
          errors = errors,
          code = "invalid_company_status"
        )

        val certificateType = Option(row(CertificateTypeIndex)).filter(_.nonEmpty)
        certificateType.foreach(value =>
          validateAllowedValue(
            lineNumber = lineNumber,
            columnIndex = CertificateTypeIndex,
            value = value,
            allowed = AllowedCertificateTypes,
            errors = errors,
            code = "invalid_certificate_type"
          )
        )

        val corporationTax = parseTaxRegimeValue(lineNumber, CorporationTaxIndex, row(CorporationTaxIndex), errors)
        val valueAddedTax  = parseTaxRegimeValue(lineNumber, ValueAddedTaxIndex, row(ValueAddedTaxIndex), errors)
        val paye           = parseTaxRegimeValue(lineNumber, PayeIndex, row(PayeIndex), errors)
        val insurancePremiumTax =
          parseTaxRegimeValue(lineNumber, InsurancePremiumTaxIndex, row(InsurancePremiumTaxIndex), errors)
        val stampDutyLandTax =
          parseTaxRegimeValue(lineNumber, StampDutyLandTaxIndex, row(StampDutyLandTaxIndex), errors)
        val stampDutyReserveTax =
          parseTaxRegimeValue(lineNumber, StampDutyReserveTaxIndex, row(StampDutyReserveTaxIndex), errors)
        val petroleumRevenueTax =
          parseTaxRegimeValue(lineNumber, PetroleumRevenueTaxIndex, row(PetroleumRevenueTaxIndex), errors)
        val customsDuties = parseTaxRegimeValue(lineNumber, CustomsDutiesIndex, row(CustomsDutiesIndex), errors)
        val exciseDuties  = parseTaxRegimeValue(lineNumber, ExciseDutiesIndex, row(ExciseDutiesIndex), errors)
        val bankLevy      = parseTaxRegimeValue(lineNumber, BankLevyIndex, row(BankLevyIndex), errors)

        if rowErrorsAtStart == errors.size then
          parsedRows += ParsedSubmissionRow(
            notification = NotificationFields(
              companyName = row(CompanyNameIndex),
              companyUtr = row(CompanyUtrIndex),
              companyCrn = row(CompanyCrnIndex),
              companyType = row(CompanyTypeIndex),
              companyStatus = row(CompanyStatusIndex),
              financialYearEndDate = row(FinancialYearEndDateIndex)
            ),
            certificate = CertificateFields(
              corporationTax = corporationTax,
              valueAddedTax = valueAddedTax,
              paye = paye,
              insurancePremiumTax = insurancePremiumTax,
              stampDutyLandTax = stampDutyLandTax,
              stampDutyReserveTax = stampDutyReserveTax,
              petroleumRevenueTax = petroleumRevenueTax,
              customsDuties = customsDuties,
              exciseDuties = exciseDuties,
              bankLevy = bankLevy,
              certificateType = certificateType,
              additionalInformation = Option(row(AdditionalInformationIndex)).filter(_.nonEmpty)
            )
          )
    }

    if errors.nonEmpty then Invalid(errors.toSeq)
    else Valid(parsedRows.toSeq)
  }

  private def normalizedDataColumns(row: Array[String]): IndexedSeq[String] =
    ExpectedHeaders.indices.map(cellValue(row, _))

  private def parseTaxRegimeValue(
      lineNumber: Int,
      columnIndex: Int,
      value: String,
      errors: ArrayBuffer[TemplateParseError]
  ): Boolean =
    value.toLowerCase match {
      case ""  => false
      case "x" => true
      case _ =>
        errors += TemplateParseError(
          line = lineNumber,
          column = Some(ExpectedHeaders(columnIndex)),
          code = "invalid_tax_regime_value",
          message = s"Line $lineNumber ${ExpectedHeaders(columnIndex)} must be 'x' or blank."
        )
        false
    }

  private def validateAllowedValue(
      lineNumber: Int,
      columnIndex: Int,
      value: String,
      allowed: Set[String],
      errors: ArrayBuffer[TemplateParseError],
      code: String
  ): Unit =
    if value.nonEmpty && !allowed.contains(value) then
      errors += TemplateParseError(
        line = lineNumber,
        column = Some(ExpectedHeaders(columnIndex)),
        code = code,
        message = s"Line $lineNumber ${ExpectedHeaders(columnIndex)} value '$value' is not valid."
      )

  private def cellValue(row: Array[String], index: Int): String =
    if index < row.length then row(index).trim else ""

}

object UploadTemplateCsvParser {

  val LinesToSkipBeforeSectionRow = 6
  private val SectionLineNumber           = 7
  private val HeaderLineNumber            = 8
  private val DataStartLineNumber         = 9

  private val SectionRowIndex = SectionLineNumber - 1
  private val HeaderRowIndex  = HeaderLineNumber - 1
  private val DataStartIndex  = DataStartLineNumber - 1

  private val NotificationSectionIndex = 1
  private val CertificateSectionIndex  = 6

  private val NotificationSectionExpected = "Notification"
  private val CertificateSectionExpected  = "Certificate"

  val ExpectedHeaders: Seq[String] = Seq(
    "Company name",
    "Company UTR",
    "Company CRN",
    "Company type",
    "Company status",
    "Financial year end date",
    "Corporation tax",
    "Value added tax",
    "PAYE",
    "Insurance premium tax",
    "Stamp duty land tax",
    "Stamp duty reserve tax",
    "Petroleum revenue tax",
    "Customs Duties",
    "Excise Duties",
    "Bank Levy",
    "Certificate type",
    "Additional information"
  )

  private val CompanyNameIndex          = 0
  private val CompanyUtrIndex           = 1
  private val CompanyCrnIndex           = 2
  private val CompanyTypeIndex          = 3
  private val CompanyStatusIndex        = 4
  private val FinancialYearEndDateIndex = 5

  private val CorporationTaxIndex      = 6
  private val ValueAddedTaxIndex       = 7
  private val PayeIndex                = 8
  private val InsurancePremiumTaxIndex = 9
  private val StampDutyLandTaxIndex    = 10
  private val StampDutyReserveTaxIndex = 11
  private val PetroleumRevenueTaxIndex = 12
  private val CustomsDutiesIndex       = 13
  private val ExciseDutiesIndex        = 14
  private val BankLevyIndex            = 15
  private val CertificateTypeIndex     = 16
  private val AdditionalInformationIndex = 17

  private val RequiredNotificationColumnIndexes = Seq(
    CompanyNameIndex,
    CompanyUtrIndex,
    CompanyCrnIndex,
    CompanyTypeIndex,
    CompanyStatusIndex,
    FinancialYearEndDateIndex
  )

  private val AllowedCompanyTypes = Set("LTD", "PLC")

  private val AllowedCompanyStatus = Set(
    "Active",
    "Dormant",
    "Administration",
    "Liquidation"
  )

  private val AllowedCertificateTypes = Set("Qualified", "Unqualified")
}
