package io.github.khanr1.tedawardparser
package repository
package parsers

import scala.xml.*
import java.time.LocalDate
import squants.Money
import cats.Applicative
import cats.syntax.all.*
import cats.MonadError
import java.time.format.DateTimeFormatter
import java.sql.Date
import cats.Monad
import cats.data.EitherT
import squants.market.*
import squants.market.defaultMoneyContext.*
import scala.util.Try
import common.*
//import io.github.khanr1.tedawardparser.repository.xmlPath.XMLPathUtils.textAt
import parsers.ParserError

trait XMLParser[F[_]]:

  def dateFormatter: DateTimeFormatter
  def parseOJSNoticeID(elem: Elem): F[Either[ParserError, OJSNoticeID]]
  def parsePublicationDate(elem: Elem): F[Either[ParserError, LocalDate]]
  def parseContractingAuthorityName(
      elem: Elem
  ): F[Either[ParserError, ContractingAuthorityName]]
  def parseContractingAuthorityCountry(
      elem: Elem
  ): F[Either[ParserError, Country]]
  def parseContractingAuthority(
      elem: Elem
  ): F[Either[ParserError, ContractingAuthority]]
  def parseContractID(elem: Elem): F[List[Either[ParserError, ContractID]]]
  def parseTenderLotTitle(elem: Elem): F[List[Either[ParserError, Title]]]
  def parseTenderLotDescription(
      elem: Elem
  ): F[List[Either[ParserError, Description]]]
  def parseTenderLotValue(
      elem: Elem
  ): F[List[Either[ParserError, squants.Money]]]
  def parseTenderLotAwardedSupplierName(
      elem: Elem
  ): F[List[Either[ParserError, AwardedSupplierName]]]
  def parseTenderLotAwardedSupplierCountry(
      elem: Elem
  ): F[List[Either[ParserError, Country]]]
  def parseTenderLotAwardedSupplier(
      elem: Elem
  ): F[List[Either[ParserError, AwardedSupplier]]]
  def parseTenderLotJustification(
      e: Elem
  ): F[List[Either[ParserError, Justification]]]

class FallBackParser[F[_]: Monad] extends XMLParser[F] {

  override def dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")

  override def parseOJSNoticeID(
      elem: Elem
  ): F[Either[ParserError, OJSNoticeID]] =
    Monad[F].pure(Left(ParserError.Unknown("No parser fit for that file")))

  override def parsePublicationDate(
      elem: Elem
  ): F[Either[ParserError, LocalDate]] =
    Monad[F].pure(Left(ParserError.Unknown("No parser fit for that file")))

  override def parseContractingAuthorityName(
      elem: Elem
  ): F[Either[ParserError, ContractingAuthorityName]] =
    Monad[F].pure(Left(ParserError.Unknown("No parser fit for that file")))

  override def parseContractingAuthorityCountry(
      elem: Elem
  ): F[Either[ParserError, Country]] =
    Monad[F].pure(Left(ParserError.Unknown("No parser fit for that file")))

  override def parseContractingAuthority(
      elem: Elem
  ): F[Either[ParserError, ContractingAuthority]] =
    Monad[F].pure(Left(ParserError.Unknown("No parser fit for that file")))

  override def parseContractID(
      elem: Elem
  ): F[List[Either[ParserError, ContractID]]] =
    Monad[F].pure(
      List(Left(ParserError.Unknown("No parser fit for that file")))
    )

  override def parseTenderLotTitle(
      elem: Elem
  ): F[List[Either[ParserError, Title]]] =
    Monad[F].pure(
      List(Left(ParserError.Unknown("No parser fit for that file")))
    )

  override def parseTenderLotDescription(
      elem: Elem
  ): F[List[Either[ParserError, Description]]] =
    Monad[F].pure(
      List(Left(ParserError.Unknown("No parser fit for that file")))
    )

  override def parseTenderLotValue(
      elem: Elem
  ): F[List[Either[ParserError, squants.Money]]] =
    Monad[F].pure(
      List(Left(ParserError.Unknown("No parser fit for that file")))
    )

  override def parseTenderLotAwardedSupplierName(
      elem: Elem
  ): F[List[Either[ParserError, AwardedSupplierName]]] =
    Monad[F].pure(
      List(Left(ParserError.Unknown("No parser fit for that file")))
    )

  override def parseTenderLotAwardedSupplierCountry(
      elem: Elem
  ): F[List[Either[ParserError, Country]]] =
    Monad[F].pure(
      List(Left(ParserError.Unknown("No parser fit for that file")))
    )

  override def parseTenderLotAwardedSupplier(
      elem: Elem
  ): F[List[Either[ParserError, AwardedSupplier]]] =
    Monad[F].pure(
      List(Left(ParserError.Unknown("No parser fit for that file")))
    )

  override def parseTenderLotJustification(
      e: Elem
  ): F[List[Either[ParserError, Justification]]] =
    Monad[F].pure(
      List(Left(ParserError.Unknown("No parser fit for that file")))
    )

}
