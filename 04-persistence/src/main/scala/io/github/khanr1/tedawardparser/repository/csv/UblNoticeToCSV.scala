package io.github.khanr1.tedawardparser
package repository
package csv

import cats.Applicative
import fs2.{Pipe, Stream}
import io.github.khanr1.tedawardparser.ublExport.UblNotice

object UblNoticeToCSV:

  def toCSV[F[_]: Applicative]: Pipe[F, UblNotice, String] =
    in =>
      val header: Stream[F, String] =
        Stream.eval(Applicative[F].pure(UblNotice.csvHeader.mkString(",")))

      val rows: Stream[F, String] = in.flatMap { notice =>
        val rowStrings = notice.toCsvRows.map(_.mkString(","))
        Stream.evalSeq(Applicative[F].pure(rowStrings))
      }

      header ++ rows
