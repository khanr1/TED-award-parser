package io.github.khanr1.tedawardparser.repository.xml.decoders.codedDataSection

import io.github.khanr1.tedawardparser.repository.xml.*

import scala.xml.Elem
import io.github.khanr1.tedawardparser.repository.xml.XMLPathUtils.*
import io.github.khanr1.tedawardparser.repository.xpath.CodedDataSectionPath

import cats.syntax.all.*

final case class NoticeDataRaw(
    ojsNumber: Either[ParserError, String],
    documentURIs: List[Either[ParserError, String]],
    langages: Either[ParserError, List[String]],
    buyerCountry: Either[ParserError, String],
    buyerURL: Either[ParserError, String],
    CPV: Either[ParserError, String],
    values: List[Either[ParserError, (String, String)]],
    referenceNotice: Either[ParserError, String]
) extends Raw

object NoticeDataRaw:
  given XMLDecoder[NoticeDataRaw] = new XMLDecoder[NoticeDataRaw] {

    override def decode(e: Elem): NoticeDataRaw = {
      val ojsNumber = e.textAtOrError(
        CodedDataSectionPath.NoticeDataPath.ojsNumberPath,
        "Official Journal Serie Number"
      )
      val documentURIs = e
        .childrenAt(CodedDataSectionPath.NoticeDataPath.documentURIPath)
        .map(x =>
          x.headOption
            .map(n => n.text.replaceAll("\\s+", " ").trim)
            .toRight(
              ParserError.MissingField(
                "URIs",
                Some(CodedDataSectionPath.NoticeDataPath.documentURIPath.show)
              )
            )
        )
      val langages = e
        .textAtOrError(
          CodedDataSectionPath.NoticeDataPath.languagePath,
          "Language"
        )
        .map(s =>
          s.trim
            .replace(",", ".")
            .split(" ")
            .toList
        )
      val buyerCountry = e
        .attrAt(CodedDataSectionPath.NoticeDataPath.buyerCountryPath)
        .toRight(
          ParserError.MissingField(
            "Buyer Country",
            Some(CodedDataSectionPath.NoticeDataPath.buyerCountryPath.show)
          )
        )
      val buyerURL = e.textAtOrError(
        CodedDataSectionPath.NoticeDataPath.buyerURLPath,
        "Buyer URL"
      )
      val CPV =
        e.textAtOrError(CodedDataSectionPath.NoticeDataPath.CPVPath, "CPV")
      val values = e
        .childrenAt(CodedDataSectionPath.NoticeDataPath.valuePath)
        .map(x => {
          val valueType = x
            .attribute("TYPE")
            .map(_.text.replaceAll("\\s+", " ").trim)
            .toRight(
              ParserError.MissingField(
                "Value type",
                Some(CodedDataSectionPath.NoticeDataPath.valuePath.show)
              )
            )
          val currency: Either[ParserError, String] = x
            .attrAt(CodedDataSectionPath.NoticeDataPath.currenctyPath)
            .toRight(
              ParserError.MissingField(
                "Currency",
                Some(CodedDataSectionPath.NoticeDataPath.currenctyPath.show)
              )
            )
            .flatMap(s =>
              Right(
                s.replace(" ", "")
                  .replace(",", ".")
              )
            )
          val amount: Either[ParserError, String] = x
            .textAtOrError(
              CodedDataSectionPath.NoticeDataPath.amountPath,
              "Amount"
            )
            .map(s => s.replace(" ", "").replace(",", "."))

          val money = (amount, currency) match
            case (Right(v), Right(c)) => Right(v + " " + c)
            case (Right(v), Left(c))  => Left(c)
            case (Left(c), _)         => Left(c)

          (valueType, money) match
            case (Right(a), Right(b)) => Right(a, b)
            case (Right(v), Left(c))  => Left(c)
            case (Left(c), _)         => Left(c)
        })
      val referenceNotice = e.textAtOrError(
        CodedDataSectionPath.NoticeDataPath.refNoticePath,
        "Notice Reference"
      )

      NoticeDataRaw(
        ojsNumber,
        documentURIs,
        langages,
        buyerCountry,
        buyerURL,
        CPV,
        values,
        referenceNotice
      )
    }

  }
