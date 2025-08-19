package io.github.khanr1.tedawardparser
package repository
package file

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
import io.github.khanr1.tedawardparser.repository.file.XMLPathUtils.textAt

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
