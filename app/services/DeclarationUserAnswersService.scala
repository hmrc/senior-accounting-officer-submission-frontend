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

import models.*
import models.Area.*
import models.certificate.CertificateWhoIsSubmitting
import pages.Page.CERTIFICATE_PATH
import pages.certificate.*
import play.api.Logging
import play.api.libs.json.*
import play.api.libs.json.Reads.*

import javax.inject.Inject

import DeclarationUserAnswersService.*

class DeclarationUserAnswersService extends Logging @Inject {
  def sanitise(userAnswers: UserAnswers): UserAnswers = {
    userAnswers.get(CertificateWhoIsSubmittingPage(NormalMode)) match {
      case Some(CertificateWhoIsSubmitting.Sao) =>
        userAnswers
          .clearTransactionArea()
          .clearCommittedAreaStandIn()
          .copyCommittedAreaToTransactionArea()
      case Some(CertificateWhoIsSubmitting.StandIn) =>
        userAnswers
          .clearTransactionArea()
          .clearCommittedAreaSao()
          .copyCommittedAreaToTransactionArea()
      case None => ???
    }
  }

  extension (userAnswers: UserAnswers) {
    def copyCommittedAreaToTransactionArea(): UserAnswers = {
      userAnswers.transformUserAnswers(
        (__ \ CERTIFICATE_PATH).json.update(
          __.read[JsObject].map { o =>
            Json.obj(TRANSACTION_PATH -> (userAnswers.data \ CERTIFICATE_PATH \ COMMITTED_PATH).getOrElse(???))
          }
        )
      )
    }

    def clearCommittedAreaStandIn(): UserAnswers = {
      userAnswers.transformUserAnswers(
        (__ \ CERTIFICATE_PATH \ COMMITTED_PATH \ standInKey).json.prune
      )
    }

    def clearCommittedAreaSao(): UserAnswers = {
      userAnswers.transformUserAnswers(
        (__ \ CERTIFICATE_PATH \ COMMITTED_PATH \ saoKey).json.prune
      )
    }

    def clearTransactionArea(): UserAnswers = {
      userAnswers.transformUserAnswers((__ \ CERTIFICATE_PATH \ TRANSACTION_PATH).json.prune)
    }

    def transformUserAnswers(transformer: Reads[JsObject]): UserAnswers = {
      userAnswers.data.transform(transformer) match {
        case JsError(error) => {
          logger.error("Json transformation error: " + error)
          ???
        }
        case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
      }
    }
  }

  def commitTransaction(userAnswers: UserAnswers): UserAnswers = {
    userAnswers.get(CertificateWhoIsSubmittingPage(TransactionMode)) match {
      case Some(CertificateWhoIsSubmitting.Sao)     => commitSaoDeclarationTransaction(userAnswers)
      case Some(CertificateWhoIsSubmitting.StandIn) => commitStandInDeclarationTransaction(userAnswers)
      case None                                     => ???
    }
  }

  private def commitSaoDeclarationTransaction(userAnswers: UserAnswers): UserAnswers = {
    (for {
      whoSubmits <- userAnswers.get(CertificateWhoIsSubmittingPage(TransactionMode))
      saoName    <- userAnswers.get(CertificateDeclarationSaoPage(TransactionMode))
    } yield {
      userAnswers
        .set(CertificateWhoIsSubmittingPage(NormalMode), whoSubmits)
        .flatMap(_.set(CertificateDeclarationSaoPage(NormalMode), saoName))
        .getOrElse(???)
    }).getOrElse(???)
  }

  private def commitStandInDeclarationTransaction(userAnswers: UserAnswers): UserAnswers = {
    (for {
      whoSubmits <- userAnswers.get(CertificateWhoIsSubmittingPage(TransactionMode))
      standIn    <- userAnswers.get(CertificateDeclarationStandInPage(TransactionMode))
    } yield {
      userAnswers
        .set(CertificateWhoIsSubmittingPage(NormalMode), whoSubmits)
        .flatMap(_.set(CertificateDeclarationStandInPage(NormalMode), standIn))
        .getOrElse(???)
    }).getOrElse(???)
  }
}

object DeclarationUserAnswersService {
  val saoKey: String             = CertificateDeclarationSaoPage(NormalMode).toString
  val standInKey: String         = CertificateDeclarationStandInPage(NormalMode).toString
  val whoIsSubmittingKey: String = CertificateWhoIsSubmittingPage(NormalMode).toString
}
