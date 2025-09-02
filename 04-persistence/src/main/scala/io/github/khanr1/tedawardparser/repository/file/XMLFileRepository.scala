package io.github.khanr1.tedawardparser
package repository
package file

import repository.NoticeRepository
import fs2.{text, Pipe}
import fs2.io.file.{Path, Files}
import scala.xml.{Elem, XML}
import cats.syntax.all.*

object XMLFileRepository {
  def make[F[_]: cats.effect.Concurrent: Files](
      path: Path
  ): NoticeRepository[F] = new NoticeRepository[F] {

    override def getAll: fs2.Stream[F, Notice] = ???
    override def getOJSID: fs2.Stream[F, OJSNoticeID] = ???

    val xmlFile = (path: Path) =>
      Files[F].walk(path).filter(p => p.extName == ".xml")

    def fileToElem: Pipe[F, Path, (Path, Elem)] = s =>
      s.evalMap(path =>
        Files[F]
          .readAll(path)
          .through(text.utf8.decode)
          .compile
          .string
          .map(content => (path.fileName, XML.loadString(content)))
      )

  }
}
