package io.github.khanr1.tedawardparser
package service

import io.github.khanr1.tedawardparser.*
import io.github.khanr1.tedawardparser.repository.NoticeRepository

import weaver.SimpleIOSuite
import cats.effect.IO
import fs2.Stream
import squants.market.Money
import squants.market.EUR

object NoticeServiceSuite extends SimpleIOSuite:

  val dummyNoticeWithLots = Notice(
    noticeID = OJSNoticeID("N-001"),
    publicationDate = java.time.LocalDate.of(2024, 5, 1),
    contractingAuthority =
      ContractingAuthority(ContractingAuthorityName("Authority A"), Country.FR),
    lots = List(
      TenderLot(
        id = ContractID("id"),
        title = Title("Super, \"Complex\" Lot"),
        description = Description("Some description,\nmultiline"),
        value = Money(12345.67, EUR),
        awardedSupplier = Some(
          AwardedSupplier(AwardedSupplierName("Supplier A, Ltd."), Country.DE)
        ),
        justification = Justification("Lowest price")
      )
    )
  )

  val dummyNoticeWithoutLots = Notice(
    noticeID = OJSNoticeID("N-002"),
    publicationDate = java.time.LocalDate.of(2024, 6, 1),
    contractingAuthority =
      ContractingAuthority(ContractingAuthorityName("Authority B"), Country.IT),
    lots = Nil
  )

  class InMemoryRepo extends NoticeRepository[IO]:
    val streamElements =
      Stream(dummyNoticeWithLots, dummyNoticeWithoutLots)
    override def getAll: Stream[IO, Notice] = streamElements

  val service = NoticeService.make[IO](new InMemoryRepo)

  test("toCSV should produce correct CSV rows with escaping") {
    val resultIO = Stream(dummyNoticeWithLots, dummyNoticeWithoutLots)
      .through(service.toCSV)
      .compile
      .toList

    resultIO.map { rows =>
      expect(
        rows.head.contains(
          "Notice ID,Publication date,Contracting authority name"
        )
      ) and
        expect(rows.exists(_.contains("\"Supplier A, Ltd.\""))) and
        expect(rows.exists(_.contains("\"Some description,\nmultiline\""))) and
        expect(rows.exists(_.contains("the notice does not have lots")))
    }

  }

  test("toCSV should produce full correct CSV output") {
    val expected =
      """Notice ID,Publication date,Contracting authority name,Contracting authority country code,ContractID,Title,Description,Value,Currency,Awarded supplier,Awarded supplier country code,Justification
        |N-001,2024-05-01,Authority A,FR,id,"Super, ""Complex"" Lot","Some description,
        |multiline",12345.67,EUR,"Supplier A, Ltd.",DE,Lowest price
        |N-002,2024-06-01,Authority B,IT,the notice does not have lots,the notice does not have lots,the notice does not have lots,the notice does not have lots,the notice does not have lots,the notice does not have lots,the notice does not have lots,the notice does not have lots""".stripMargin

    Stream(dummyNoticeWithLots, dummyNoticeWithoutLots)
      .through(service.toCSV)
      .compile
      .toList
      .map(_.mkString("\n"))
      .map(actual => expect.same(expected, actual))
  }
