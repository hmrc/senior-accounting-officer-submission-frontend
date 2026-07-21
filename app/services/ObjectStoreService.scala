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

import services.ObjectStoreService.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.ObjectSummary
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject

class ObjectStoreService @Inject() (objectStoreClient: PlayObjectStoreClient)(using ec: ExecutionContext) {
  def isNotificationPdfAvailable(notificationReference: String)(using hc: HeaderCarrier): Future[Boolean] = {
    objectStoreClient
      .listObjects(
        path = Path.Directory(s"/$objectStoreOwner/$notificationReference/"),
        owner = objectStoreOwner
      )
      .map { objectListing =>
        objectListing.objectSummaries match {
          case objectSummaries if objectSummaries.exists { case ObjectSummary(Path.File(_, fileName), _, _) =>
                fileName == s"${notificationReference}_SAO_Notification.pdf"
              } =>
            true
          case _ => false
        }
      }
  }

  def isCertificatePdfAvailable(certificateReference: String)(using hc: HeaderCarrier): Future[Boolean] = {
    objectStoreClient
      .listObjects(
        path = Path.Directory(s"/$objectStoreOwner/$certificateReference/"),
        owner = objectStoreOwner
      )
      .map { objectListing =>
        objectListing.objectSummaries match {
          case objectSummaries if objectSummaries.exists { case ObjectSummary(Path.File(_, fileName), _, _) =>
                fileName == s"${certificateReference}_SAO_Certificate.pdf"
              } =>
            true
          case _ => false
        }
      }
  }
}

object ObjectStoreService {
  val objectStoreOwner = "senior-accounting-officer"
}
