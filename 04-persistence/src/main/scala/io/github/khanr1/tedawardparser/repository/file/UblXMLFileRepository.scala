package io.github.khanr1.tedawardparser
package repository
package file

import cats.effect.Sync
import cats.syntax.flatMap.*
import fs2.io.file.{Files, Path}
import fs2.Stream
import scala.xml.XML
import io.github.khanr1.tedawardparser.repository.xml.{UblNoticeAssembler, UblNoticeMapper}
import io.github.khanr1.tedawardparser.ublExport.UblNotice

object UblXMLFileRepository:

  def stream[F[_]: Sync: Files](dir: Path): Stream[F, UblNotice] =
    Files[F]
      .list(dir)
      .evalFilter(Files[F].isRegularFile)
      .evalMapFilter { path =>
        Files[F]
          .readAll(path)
          .through(fs2.text.utf8.decode)
          .compile
          .string
          .flatMap(content => Sync[F].delay(XML.loadString(content)))
          .flatMap { elem =>
            Sync[F].delay(UblNoticeAssembler.decode(elem)).flatMap {
              case Some(parsed) =>
                Sync[F].delay(Some(UblNoticeMapper.toDomain(parsed)))
              case None =>
                Sync[F].delay {
                  println(s"skip (not UBL) ${path.fileName}")
                  None
                }
            }
          }
      }
