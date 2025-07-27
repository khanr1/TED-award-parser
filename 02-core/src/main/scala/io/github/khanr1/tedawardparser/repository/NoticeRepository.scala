package io.github.khanr1.tedawardparser
package repository

trait NoticeRepository[F[_]] {
  // stream all A element
  def getAll: fs2.Stream[F, Notice]
}
