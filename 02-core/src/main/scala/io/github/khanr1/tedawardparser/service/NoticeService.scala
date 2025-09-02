package io.github.khanr1.tedawardparser
package service

import cats.Applicative
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.repository.NoticeRepository
import java.time.LocalDate
import squants.Money

trait NoticeService[F[_]: Applicative] {
  // def toCSV: fs2.Pipe[F, Notice, String]
  def getAll: fs2.Stream[F, Notice]
  def getOJSID: fs2.Stream[F, OJSNoticeID]
  def getPublicationDate: fs2.Stream[F, LocalDate]
  def getContractingAuthorityName: fs2.Stream[F, ContractingAuthorityName]
  def getContractingAuthorityCountry: fs2.Stream[F, Country]
  def getContractingAuthority: fs2.Stream[F, ContractingAuthority]
  def getContractIDs: fs2.Stream[F, List[ContractID]]
  def getTenderLotTitles: fs2.Stream[F, List[Title]]
  def getTenderLotDescriptions: fs2.Stream[F, List[Description]]
  def getTenderLotValue: fs2.Stream[F, List[squants.Money]]
  def getTenderLotAwardedSupplierName: fs2.Stream[F, List[AwardedSupplierName]]
  def getTenderLotAwardedSupplierCountry: fs2.Stream[F, List[Country]]
  def getTenderLotAwardedSupplier: fs2.Stream[F, List[AwardedSupplier]]
  def getTenderLotJustification: fs2.Stream[F, Justification]

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
  def toCSV: fs2.Pipe[F, Notice, String] = in => {
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

object NoticeService:
  def make[F[_]: Applicative](repo: NoticeRepository[F]): NoticeService[F] =
    new NoticeService[F] {

      override def getAll: fs2.Stream[F, Notice] = repo.getAll

      override def getOJSID: fs2.Stream[F, OJSNoticeID] = repo.getOJSID

      override def getPublicationDate: fs2.Stream[F, LocalDate] =
        repo.getPublicationDate

      override def getContractingAuthorityName
          : fs2.Stream[F, ContractingAuthorityName] =
        repo.getContractingAuthorityName

      override def getContractingAuthorityCountry: fs2.Stream[F, Country] =
        repo.getContractingAuthorityCountry

      override def getContractingAuthority
          : fs2.Stream[F, ContractingAuthority] = repo.getContractingAuthority

      override def getContractIDs: fs2.Stream[F, List[ContractID]] =
        repo.getContractIDs

      override def getTenderLotTitles: fs2.Stream[F, List[Title]] =
        repo.getTenderLotTitles

      override def getTenderLotDescriptions: fs2.Stream[F, List[Description]] =
        repo.getTenderLotDescriptions

      override def getTenderLotValue: fs2.Stream[F, List[Money]] =
        repo.getTenderLotValue

      override def getTenderLotAwardedSupplierName
          : fs2.Stream[F, List[AwardedSupplierName]] =
        repo.getTenderLotAwardedSupplierName

      override def getTenderLotAwardedSupplierCountry
          : fs2.Stream[F, List[Country]] =
        repo.getTenderLotAwardedSupplierCountry

      override def getTenderLotAwardedSupplier
          : fs2.Stream[F, List[AwardedSupplier]] =
        repo.getTenderLotAwardedSupplier

      override def getTenderLotJustification: fs2.Stream[F, Justification] =
        repo.getTenderLotJustification

    }
