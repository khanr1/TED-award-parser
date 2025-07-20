package io.github.khanr1.tedawardparser

import cats.Show

opaque type PublicationDate = String
object PublicationDate:
  def apply(s: String): PublicationDate = s

  given show: Show[PublicationDate] = Show.show(n => n.value)

  extension (d: PublicationDate) def value: String = d

opaque type NoticeNumber = String
object NoticeNumber:
  def apply(s: String): NoticeNumber = s

  given show: Show[NoticeNumber] = Show.show(n => n.value)

  extension (n: NoticeNumber) def value: String = n

enum NoticeType:
  case CAN
  case CN
  case VEAT
  case PIN
  case UNKNOWN

object NoticeType:
  private val noticaTypeLookUP: Map[NoticeType, String] = Map(
    CAN -> "contract award notice",
    VEAT -> "voluntary ex ante transparency notice",
    PIN -> "prior information notice without call for competition",
    CN -> "contract notice"
  )
  given Show[NoticeType] = Show.show(typ => noticaTypeLookUP(typ))
  def toDomain(s: String): NoticeType =
    noticaTypeLookUP.map((k, v) => (v, k)).getOrElse(s.toLowerCase(), UNKNOWN)

enum ProcurementProcess:
  case OpenProcedure
  case ContractAwardWithoutPriorPublication
  case NegotiatedWithoutAPriorCallForCompetition
  case NegotiatedProcedure

object ProcurementProcess:
  private val procProsLookup: Map[ProcurementProcess, String] = Map(
    OpenProcedure -> "open procedure",
    ContractAwardWithoutPriorPublication -> "contract award without prior publication",
    NegotiatedWithoutAPriorCallForCompetition -> "negotiated without a prior call for competition",
    NegotiatedProcedure -> "negociated procedure"
  )
  given show: Show[ProcurementProcess] = Show.show(proc => procProsLookup(proc))
