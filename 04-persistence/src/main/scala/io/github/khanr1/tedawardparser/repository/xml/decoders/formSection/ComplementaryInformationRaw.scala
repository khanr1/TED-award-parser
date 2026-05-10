package io.github.khanr1.tedawardparser.repository.xml
package decoders
package formSection

import scala.xml.Elem
import io.github.khanr1.tedawardparser.repository.xml.XMLPathUtils.*
import io.github.khanr1.tedawardparser.repository.xpath.{FormSectionPathR208, FormSectionPathR209}

final case class ComplementaryInformationRaw(
    additionalInformation: Either[ParserError, String],
    relatesToEuProject: Option[String],
    noticeDispatchDate: Either[ParserError, String],
    appealBodyName: Either[ParserError, String],
    justification: Either[ParserError, String]
) extends Raw

object ComplementaryInformationDecoderR208F03:
  given XMLDecoder[ComplementaryInformationRaw] =
    new XMLDecoder[ComplementaryInformationRaw]:
      override def decode(e: Elem): ComplementaryInformationRaw =
        val path = FormSectionPathR208.F03.ComplementaryInformationPath
        ComplementaryInformationRaw(
          e.textAtOrError(path.additionalInformationPath, "Additional Information"),
          e.textAt(path.relatesToEuProjectYesPath),
          e.textAtOrError(path.noticeDispatchDatePath, "Dispatch Date"),
          e.textAtOrError(path.appealBodyNamePath, "Appeal Body Name"),
          e.textAtOrError(path.justificationPath, "Justification")
        )

object ComplementaryInformationDecoderR208F15:
  given XMLDecoder[ComplementaryInformationRaw] =
    new XMLDecoder[ComplementaryInformationRaw]:
      override def decode(e: Elem): ComplementaryInformationRaw =
        val path = FormSectionPathR208.F15.ComplementaryInformationPath
        ComplementaryInformationRaw(
          e.textAtOrError(path.additionalInformationPath, "Additional Information"),
          e.textAt(path.relatesToEuProjectYesPath),
          e.textAtOrError(path.noticeDispatchDatePath, "Dispatch Date"),
          e.textAtOrError(path.appealBodyNamePath, "Appeal Body Name"),
          e.textAtOrError(path.justificationPath, "Justification")
        )

object ComplementaryInformationDecoderR209F03:
  given XMLDecoder[ComplementaryInformationRaw] =
    new XMLDecoder[ComplementaryInformationRaw]:
      override def decode(e: Elem): ComplementaryInformationRaw =
        val path = FormSectionPathR209.F03.ComplementaryInformationPath
        ComplementaryInformationRaw(
          e.textAtOrError(path.additionalInformationPath, "Additional Information"),
          None,
          e.textAtOrError(path.noticeDispatchDatePath, "Dispatch Date"),
          e.textAtOrError(path.appealBodyNamePath, "Appeal Body Name"),
          e.textAtOrError(path.justificationPath, "Justification")
        )

object ComplementaryInformationDecoderR209F15:
  given XMLDecoder[ComplementaryInformationRaw] =
    new XMLDecoder[ComplementaryInformationRaw]:
      override def decode(e: Elem): ComplementaryInformationRaw =
        val path = FormSectionPathR209.F15.ComplementaryInformationPath
        ComplementaryInformationRaw(
          e.textAtOrError(path.additionalInformationPath, "Additional Information"),
          None,
          e.textAtOrError(path.noticeDispatchDatePath, "Dispatch Date"),
          e.textAtOrError(path.appealBodyNamePath, "Appeal Body Name"),
          e.textAtOrError(path.justificationPath, "Justification")
        )
