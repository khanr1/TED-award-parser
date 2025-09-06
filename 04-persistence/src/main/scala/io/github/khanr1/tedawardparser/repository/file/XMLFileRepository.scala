package io.github.khanr1.tedawardparser
package repository
package file

import repository.NoticeRepository
import fs2.{text, Pipe}
import fs2.io.file.{Path, Files}
import scala.xml.{Elem, XML}
import cats.syntax.all.*
import scala.collection.View.Elems
import squants.Money
import java.time.LocalDate
import cats.data.EitherT
import io.github.khanr1.tedawardparser.models.PartialNotice
import io.github.khanr1.tedawardparser.repository.file.ParserSelect.detect

object XMLFileRepository {
  def make[F[_]: cats.effect.Concurrent: Files](
      path: Path
  ): NoticeRepository[F] = new NoticeRepository[F] {

    override def getAll: fs2.Stream[F, PartialNotice] = xmlElems.map { e =>

      val parser = detect(e)

      ???

    }

    private val xmlFile = (path: Path) =>
      Files[F].walk(path).filter(p => p.extName == ".xml")

    private def fileToElem: Pipe[F, Path, Elem] = s =>
      s.evalMap(path =>
        Files[F]
          .readAll(path)
          .through(text.utf8.decode)
          .compile
          .string
          .map(content => XML.loadString(content))
      )

    private val xmlElems: fs2.Stream[F, Elem] =
      xmlFile(path).through(fileToElem)

  }
}
