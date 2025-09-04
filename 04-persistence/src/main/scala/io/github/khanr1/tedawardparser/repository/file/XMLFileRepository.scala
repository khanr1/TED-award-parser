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

object XMLFileRepository {
  def make[F[_]: cats.effect.Concurrent: Files](
      path: Path
  ): NoticeRepository[F] = new NoticeRepository[F] {

    override def getPublicationDate: fs2.Stream[F, LocalDate] = ???

    override def getContractingAuthorityName
        : fs2.Stream[F, ContractingAuthorityName] = ???

    override def getContractingAuthorityCountry: fs2.Stream[F, Country] = ???

    override def getContractingAuthority: fs2.Stream[F, ContractingAuthority] =
      ???

    override def getContractIDs: fs2.Stream[F, List[ContractID]] = ???

    override def getTenderLotTitles: fs2.Stream[F, List[Title]] = ???

    override def getTenderLotDescriptions: fs2.Stream[F, List[Description]] =
      ???

    override def getTenderLotValue: fs2.Stream[F, List[Money]] = ???

    override def getTenderLotAwardedSupplierName
        : fs2.Stream[F, List[AwardedSupplierName]] = ???

    override def getTenderLotAwardedSupplierCountry
        : fs2.Stream[F, List[Country]] = ???

    override def getTenderLotAwardedSupplier
        : fs2.Stream[F, List[AwardedSupplier]] = ???

    override def getTenderLotJustification: fs2.Stream[F, Justification] = ???

    override def getAll: fs2.Stream[F, Notice] = ???
    override def getOJSID: fs2.Stream[F, OJSNoticeID] = ???

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
