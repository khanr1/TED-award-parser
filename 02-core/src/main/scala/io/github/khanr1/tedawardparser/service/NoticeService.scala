package io.github.khanr1.tedawardparser
package service

import cats.Applicative
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.repository.NoticeRepository
import java.time.LocalDate
import squants.Money
import io.github.khanr1.tedawardparser.models.PartialNotice

trait NoticeService[F[_]: Applicative] {
  // def toCSV: fs2.Pipe[F, Notice, String]
  def getAll: fs2.Stream[F, PartialNotice]

}

object NoticeService:
  def make[F[_]: Applicative](repo: NoticeRepository[F]): NoticeService[F] =
    new NoticeService[F] {

      override def getAll: fs2.Stream[F, PartialNotice] = repo.getAll

    }
