package io.github.khanr1.tedawardparser.repository.parsers.tedexport.codedDataSection

import cats.syntax.all.*
import io.github.khanr1.tedawardparser.common.Country
import io.github.khanr1.tedawardparser.repository.parsers.*
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.{
  attrAt,
  childrenAt,
  textAt,
  textAtOrError
}
import io.github.khanr1.tedawardparser.tedexport.codedDataSection.*

import scala.xml.Elem
import squants.market.*
import squants.market.defaultMoneyContext.*

object PartialNoticeDataDecoder {
  val root: XMLPath = XMLPath("CODED_DATA_SECTION") / "NOTICE_DATA"
  val ojsNumberPath = root / "NO_DOC_OJS"
  val documentURIPath = root / "URI_LIST" / "URI_DOC"
  val languagePath = root / "LG_ORIG"
  val buyerCountryPath = root / "ISO_COUNTRY" attr ("VALUE")
  val buyerURLPath = root / "IA_URL_GENERAL"
  val CPVPath = root / "ORIGINAL_CPV"
  val valuePath = root / "VALUES_LIST" / "VALUES"
  val amountPath = XMLPath("SINGLE_VALUE") / "VALUE"
  val currenctyPath = amountPath attr ("CURRENCY")
  val refNoticePath = root / "REF_NOTICE" / "NO_DOC_OJS"

  given XMLDecoder[PartialNoticeData[ParserError]] =
    new XMLDecoder[PartialNoticeData[ParserError]] {

      override def decode(
          e: Elem
      ): Either[ParserError, PartialNoticeData[ParserError]] = {
        val ojsNumber = e
          .textAtOrError(ojsNumberPath, "Ojs Number")
          .map(s => OjsNumber(s))
        val documentURIs = e
          .childrenAt(documentURIPath)
          .map(x =>
            x.headOption
              .map(y => URL(y.text))
              .toRight(
                ParserError.MissingField("URI", Some(documentURIPath.show))
              )
          )

        val language = e
          .textAtOrError(languagePath, "Language")
          .map(s =>
            s.trim
              .replace(",", ".")
              .split(" ")
              .toList
              .map(x => Language(x))
          )
        val buyerCountry = e
          .attrAt(buyerCountryPath)
          .map(c => Country.toDomain(c))
          .toRight(
            ParserError.MissingField(
              "Buyer Country",
              Some((root / "ISO_COUNTRY" attr ("VALUE")).show)
            )
          )
        val buyerURL =
          e.textAtOrError(
            buyerURLPath,
            "Buyer URL"
          ).map(URL(_))

        val cpv = e.textAtOrError(CPVPath, "CPV").map(CPV(_))

        val values = e
          .childrenAt(valuePath)
          .map(x => {
            val valueType = x
              .attribute("TYPE")
              .map(_.text)
              .toRight(ParserError.MissingField("Value Type"))
            val currency: Either[ParserError, Currency] = x
              .attrAt(currenctyPath)
              .toRight(
                ParserError.MissingField("Currency", Some(currenctyPath.show))
              )
              .flatMap(s =>
                (Currency(s.replace(" ", "").replace(",", "."))(
                  defaultMoneyContext
                ).toEither).leftMap(error =>
                  ParserError.InvalidFormat("Currency", "valid currenty", s)
                )
              )
            val amount: Either[ParserError, BigDecimal] = x
              .textAtOrError(amountPath, "Amount")
              .flatMap(s =>
                Either
                  .catchNonFatal(
                    BigDecimal(s.replace(" ", "").replace(",", "."))
                  )
                  .leftMap(t =>
                    ParserError.InvalidFormat("Amount", "valid value", s)
                  )
              )
            val money = (amount, currency) match
              case (Right(v), Right(c)) => Right(Money(v, c))
              case (Right(v), Left(c))  => Left(c)
              case (Left(c), _)         => Left(c)

            (valueType, money) match
              case (Right(a), Right(b)) => Right(a, b)
              case (Right(v), Left(c))  => Left(c)
              case (Left(c), _)         => Left(c)
          })
        val refNotice = e
          .textAtOrError(
            refNoticePath,
            "Notice Reference"
          )
          .map(OjsNumber(_))
        Right(
          PartialNoticeData(
            ojsNumber,
            documentURIs,
            language,
            buyerCountry,
            buyerURL,
            cpv,
            values,
            refNotice
          )
        )

      }

    }
}
