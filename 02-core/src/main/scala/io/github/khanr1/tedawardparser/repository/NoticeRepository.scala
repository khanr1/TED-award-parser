package io.github.khanr1.tedawardparser
package repository

import java.time.LocalDate
import io.github.khanr1.tedawardparser.models.PartialNotice

trait NoticeRepository[F[_]] {
  // stream all A element
  def getAll: fs2.Stream[F, PartialNotice]

}
