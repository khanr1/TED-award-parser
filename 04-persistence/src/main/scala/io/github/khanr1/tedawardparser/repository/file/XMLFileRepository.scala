package io.github.khanr1.tedawardparser
package repository
package file

import cats.effect.Sync
import cats.syntax.flatMap.*
import fs2.io.file.{Files, Path}
import fs2.Stream
import scala.xml.{Elem, XML}
import io.github.khanr1.tedawardparser.repository.xml.{NoticeAssembler, ParsedNotice}

object XMLFileRepository:

  def stream[F[_]: Sync: Files](dir: Path): Stream[F, ParsedNotice] =
    Files[F]
      .list(dir)
      .evalFilter(Files[F].isRegularFile)
      .evalMapFilter { path =>
        Files[F]
          .readAll(path)
          .through(fs2.text.utf8.decode)
          .compile
          .string
          .flatMap { content =>
            Sync[F].delay(XML.loadString(content))
          }
          .flatMap { elem =>
            Sync[F].delay(NoticeAssembler.decode(elem)).flatMap {
              case Some(notice) => Sync[F].pure(Some(notice))
              case None =>
                Sync[F].delay {
                  println(s"skip parsing ${path.fileName}")
                  None
                }
            }
          }
      }
