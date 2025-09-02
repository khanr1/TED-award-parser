package io.github.khanr1.tedawardparser
package repository

import java.time.LocalDate

trait NoticeRepository[F[_]] {
  // stream all A element
  def getAll: fs2.Stream[F, Notice]
  def getOJSID: fs2.Stream[F, OJSNoticeID]
  def getPublicationDate: fs2.Stream[F, LocalDate]
  def getContractingAuthorityName: fs2.Stream[F, ContractingAuthorityName]
  def getContractingAuthorityCountry: fs2.Stream[F, Country]
  def getContractingAuthority: fs2.Stream[F, ContractingAuthority]
  def getContractIDs: fs2.Stream[F, List[ContractID]]
  def getTenderLotTitles: fs2.Stream[F, List[Title]]
  def getTenderLotDescriptions: fs2.Stream[F, List[Description]]
  def getTenderLotValue: fs2.Stream[F, List[squants.Money]]
  def getTenderLotAwardedSupplierName: fs2.Stream[F, List[AwardedSupplierName]]
  def getTenderLotAwardedSupplierCountry: fs2.Stream[F, List[Country]]
  def getTenderLotAwardedSupplier: fs2.Stream[F, List[AwardedSupplier]]
  def getTenderLotJustification: fs2.Stream[F, Justification]

}
