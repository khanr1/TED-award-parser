package io.github.khanr1.tedawardparser
package service

import cats.Applicative
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.repository.NoticeRepository

trait NoticeService[F[_]] {
  def toCSV: fs2.Pipe[F, Notice, String]
  def getOJSID: fs2.Stream[F, OJSNoticeID]
  def getAll: fs2.Stream[F, Notice]
}

object NoticeService:
  def make[F[_]: Applicative](repo: NoticeRepository[F]): NoticeService[F] =
    new NoticeService[F] {

      private def escape(s: String): String =
        if s.contains(",") || s.contains("\"") || s.contains("\n") then
          "\"" + s.replace("\"", "\"\"") + "\""
        else s

      private val csvHeader: List[String] = List(
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

      private def rowForLot(
          notice: Notice,
          tenderLot: TenderLot
      ): List[String] = {
        val (supplier, country) = tenderLot.awardedSupplier match
          case None          => ("no awarded supplier", "no awarded supplier")
          case Some(awardee) => (awardee.name.show, awardee.countryCode.show)

        List(
          notice.noticeID.show,
          notice.publicationDate.toString(),
          notice.contractingAuthority.name.toString(),
          notice.contractingAuthority.countryCode.toString(),
          tenderLot.id.show,
          tenderLot.title.show,
          tenderLot.description.show,
          tenderLot.value.amount.toString,
          tenderLot.value.currency.code,
          supplier,
          country,
          tenderLot.justification.show
        ).map(escape(_))
      }

      private def rowsForNotice(notice: Notice): List[List[String]] = {
        notice.lots match
          case Nil =>
            List(
              List(
                notice.noticeID.show,
                notice.publicationDate.toString(),
                notice.contractingAuthority.name.toString(),
                notice.contractingAuthority.countryCode.toString()
              ) ++ List.fill(8)("the notice does not have lots")
            )
          case lots => lots.map(lot => rowForLot(notice, lot))
      }

      override def getOJSID: fs2.Stream[F, OJSNoticeID] = repo.getOJSID
      override def getAll: fs2.Stream[F, Notice] = repo.getAll

      override def toCSV: fs2.Pipe[F, Notice, String] = in => {
        val header: fs2.Stream[F, String] =
          fs2.Stream.eval(Applicative[F].pure(csvHeader.mkString(",")))

        val row: fs2.Stream[F, String] = in.flatMap { notice =>
          val rowsString =
            rowsForNotice(notice)
              .map(_.mkString(","))

          fs2.Stream.evalSeq(Applicative[F].pure(rowsString))
        }

        header ++ row
      }

    }
