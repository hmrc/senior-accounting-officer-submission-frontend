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

package controllers

import controllers.actions.*
import models.upload.ParsedSubmissionRow
import models.upload.TemplateParseError
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import services.UpscanService
import services.UpscanService.State
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.NotificationUploadSuccessView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class NotificationUploadSuccessController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    upscanService: UpscanService,
    view: NotificationUploadSuccessView
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val logger = Logger(getClass)

  def onPageLoad(key: Option[String]): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      upscanService.fileUploadState(request.userAnswers, key).flatMap {
        case State.NoReference =>
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case State.WaitingForUpscan =>
          Future.successful(Ok(view()))
        case State.QuarantinedByUpscan =>
          Future.successful(Redirect(routes.NotificationUploadFormController.onPageLoad()))
        case State.RejectedByUpscan =>
          Future.successful(Redirect(routes.NotificationUploadFormController.onPageLoad()))
        case State.UnknownUpscanError =>
          Future.successful(Redirect(routes.NotificationUploadFormController.onPageLoad()))
        case State.DownloadFromUpscanFailed(response) =>
          logger.warn(s"Failed to download uploaded template from Upscan: ${response.status}")
          Future.successful(Redirect(routes.NotificationUploadFormController.onPageLoad()))
        case State.ValidationFailed(errors) =>
          logValidationErrors(errors)
          Future.successful(Redirect(routes.NotificationUploadFormController.onPageLoad()))
        case State.Result(reference, rows) =>
          logParsedRows(reference, rows)
          Future.successful(Redirect(routes.SubmitNotificationStartController.onPageLoad()))
      }
    }

  private def logValidationErrors(errors: Seq[TemplateParseError]): Unit = {
    val widths = Seq(6, 24, 26, 70)
    val header = formatTableLine(widths, Seq("line", "column", "code", "message"))
    val lines  = errors.map { error =>
      formatTableLine(
        widths,
        Seq(
          error.line.toString,
          error.column.getOrElse("-"),
          error.code,
          error.message
        )
      )
    }
    logger.warn(
      s"""Uploaded template failed validation with ${errors.size} error(s)
         |$header
         |${"-" * header.length}
         |${lines.mkString("\n")}""".stripMargin
    )
  }

  private def logParsedRows(reference: String, rows: Seq[ParsedSubmissionRow]): Unit = {
    val widths = Seq(5, 36, 12, 10, 6, 16, 12, 28)
    val header = formatTableLine(
      widths,
      Seq("row", "companyName", "utr", "crn", "type", "status", "financialYearEndDate", "certificateType")
    )
    val lines = rows.zipWithIndex.map { case (row, index) =>
      formatTableLine(
        widths,
        Seq(
          (index + 1).toString,
          row.notification.companyName,
          row.notification.companyUtr.value,
          row.notification.companyCrn.map(_.value).getOrElse("-"),
          row.notification.companyType.toString,
          row.notification.companyStatus.toString,
          row.notification.financialYearEndDateDisplay,
          row.certificate.certificateType.map(_.toString).getOrElse("-")
        )
      )
    }
    logger.info(
      s"""Uploaded template parsed successfully, reference=$reference, rows=${rows.size}
         |$header
         |${"-" * header.length}
         |${lines.mkString("\n")}""".stripMargin
    )
  }

  private def formatTableLine(widths: Seq[Int], values: Seq[String]): String =
    values.zip(widths).map { case (value, width) => formatCell(value, width) }.mkString(" | ")

  private def formatCell(value: String, width: Int): String = {
    val normalized = Option(value).map(_.replaceAll("\\s+", " ").trim).filter(_.nonEmpty).getOrElse("-")
    val truncated  = if normalized.length <= width then normalized else s"${normalized.take(width - 3)}..."
    truncated + (" " * (width - truncated.length))
  }
}
