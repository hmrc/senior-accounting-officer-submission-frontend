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

package models

final case class PaginationResult[T](
    items: Seq[T],
    currentPage: Int,
    pageCount: Int,
    totalItems: Int
)

object CompanyPagination {
  def apply[T](items: Seq[T], requestedPage: Int, pageSize: Int): PaginationResult[T] = {
    require(pageSize > 0)

    val pageCount   = (items.size + pageSize - 1) / pageSize
    val currentPage =
      if pageCount == 0 then 1
      else requestedPage.max(1).min(pageCount)

    PaginationResult(
      items = items.slice((currentPage - 1) * pageSize, currentPage * pageSize),
      currentPage = currentPage,
      pageCount = pageCount,
      totalItems = items.size
    )
  }
}
