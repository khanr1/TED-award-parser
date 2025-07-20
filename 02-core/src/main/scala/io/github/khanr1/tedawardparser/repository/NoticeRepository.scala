package io.github.khanr1.tedawardparser
package repository

trait NoticeRepository[F[_], A] {
  // stream all A element
  def streamElements: fs2.Stream[F, A]
}
