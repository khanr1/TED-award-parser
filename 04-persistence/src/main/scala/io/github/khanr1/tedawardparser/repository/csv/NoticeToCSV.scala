package io.github.khanr1.tedawardparser
package repository
package csv

import cats.Applicative
import fs2.{Pipe, Stream}
import io.github.khanr1.tedawardparser.tedExport.Notice

object NoticeToCSV:

  def toCSV[F[_]: Applicative]: Pipe[F, Notice, String] =
    in =>
      val headerRow: Stream[F, String] =
        Stream.eval(Applicative[F].pure(Notice.csvHeader.mkString(",")))

      val rows: Stream[F, String] = in.flatMap { notice =>
        val rowStrings = notice.toCsvRows.map(_.mkString(","))
        Stream.evalSeq(Applicative[F].pure(rowStrings))
      }

      headerRow ++ rows
