package io.github.khanr1.tedawardparser

import cats.Show

opaque type NoticeNumber = String
object NoticeNumber:
  def apply(s: String): NoticeNumber = s

  given show: Show[NoticeNumber] = Show.show(n => n.value)

  extension (n: NoticeNumber) def value: String = n

enum NoticeType:
  case CAN
  case VEAT

object NoticeType:
  private val noticaTypeLookUP: Map[NoticeType, String] = Map(
    CAN -> "contract award notice",
    VEAT -> "voluntary ex ante transparency notice"
  )

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
