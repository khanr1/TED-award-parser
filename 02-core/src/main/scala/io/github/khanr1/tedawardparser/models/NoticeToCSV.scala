package io.github.khanr1.tedawardparser
package models

import cats.syntax.all.*
import cats.Applicative
import cats.Show

object NoticeToCSV {

  extension [A: Show, B: Show](e: Either[A, B])
    def plainShow: String = e.fold(x => x.show, y => y.show)

  private val headerNotice: List[String] = List(
    "Notice ID",
    "Publication date",
    "Contracting authority name",
    "Contracting authority country code",
    "ContractID",
    "Title",
    "Description",
    "Value",
    "Currency",
    "Awarded supplier",
    "Awarded supplier country code",
    "Justification"
  )
  private def escape(s: String): String =
    if s.contains(",") || s.contains("\"") || s.contains("\n") then
      "\"" + s.replace("\"", "\"\"") + "\""
    else s
  private def rowForPartialLot(
      notice: PartialNotice,
      lot: PartialTenderLot
  ): List[String] = {
    val (supplier, country) = lot.awardedSupplier match
      case Some(value) => (value.name.plainShow, value.countryCode.plainShow)
      case None        => ("no awarded supplier", "no awarded supplier")

    List(
      notice.noticeID.plainShow,
      notice.publicationDate.plainShow,
      notice.contractingAuthority.name.plainShow,
      notice.contractingAuthority.country.plainShow,
      lot.id.plainShow,
      lot.title.plainShow,
      lot.description.plainShow,
      lot.value.map(x => x.amount).plainShow,
      lot.value.map(x => x.currency.code).plainShow,
      supplier,
      country,
      lot.justification.plainShow
    ).map(escape(_))

  }

  private def rowsForNotice(notice: PartialNotice): List[List[String]] = {
    notice.lots match
      case Nil =>
        List(
          List(
            notice.noticeID.plainShow,
            notice.publicationDate.plainShow,
            notice.contractingAuthority.name.plainShow,
            notice.contractingAuthority.country.plainShow
          ) ++ List.fill(headerNotice.length)("the notice does not have lots")
        )
      case lots => lots.map(lot => rowForPartialLot(notice, lot))
  }

  def toCSV[F[_]: Applicative]: fs2.Pipe[F, PartialNotice, String] =
    in => {
      val header: fs2.Stream[F, String] =
        fs2.Stream.eval(Applicative[F].pure(headerNotice.mkString(",")))

      val row: fs2.Stream[F, String] = in.flatMap { notice =>
        val rowsString =
          rowsForNotice(notice)
            .map(_.mkString(","))

        fs2.Stream.evalSeq(Applicative[F].pure(rowsString))
      }

      header ++ row
    }

}
