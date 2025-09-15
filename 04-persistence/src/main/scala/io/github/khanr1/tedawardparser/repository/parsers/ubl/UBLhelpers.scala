package io.github.khanr1.tedawardparser
package repository
package parsers
package ubl

import scala.util.matching.Regex

object UBLhelpers {
  def formatE(ojsId: String, ojsNoticeId: String): Either[String, String] =
    val OjsId: Regex = """^\s*(\d+)\s*/\s*(\d{4})\s*$""".r
    val NoticeId: Regex = """^\s*0*(\d+)\s*-(\d{4})$""".r
    val (issue, year) = ojsId match
      case OjsId(i, y) => (i, y)
      case _ =>
        return Left(s"Invalid ojs-id: '$ojsId' (expected like '35/2025')")
    val notice = ojsNoticeId match
      case NoticeId(n, y) if n.nonEmpty => n
      case _ =>
        return Left(
          s"Invalid ojs-notice-id: '$ojsNoticeId' (expected like 0013445-2025)"
        )
    Right(s"$year/S $issue-$notice")
}
