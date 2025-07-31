package io.github.khanr1.tedawardparser
package repository
package file

import repository.NoticeRepository

object XMLFileRepository {
  def make[F[_]]: NoticeRepository[F] = new NoticeRepository[F] {

    override def getAll: fs2.Stream[F, Notice] = ???

  }
}
