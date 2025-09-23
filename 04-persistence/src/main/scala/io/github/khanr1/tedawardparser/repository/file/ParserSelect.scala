package io.github.khanr1.tedawardparser
package repository
package file

import io.github.khanr1.tedawardparser.repository.parsers.*
import scala.xml.Elem
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.attrAt
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.rootAttr
import cats.Monad
import squants.Money
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ParserSelect {

  private val ted: String = "TED_EXPORT"
  private val version: XMLPath = XMLPath("TED_EXPORT").attr("VERSION")
  private val ublLabel: String = "ContractAwardNotice"

  def detect[F[_]: Monad](elem: Elem): XMLParser[F] = {
    val isTedExport = elem.label == ted
    val isUBL = elem.label == ublLabel

    if isTedExport then
      elem.rootAttr("VERSION") match
        case Some(value) => new r209.TedExportR209[F]
        case None        => new r208.TedExportR208[F]
    else if isUBL then new ubl.UBLParser[F]
    else new FallBackParser[F]
  }

}
