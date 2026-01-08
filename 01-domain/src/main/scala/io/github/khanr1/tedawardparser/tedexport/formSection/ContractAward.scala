package io.github.khanr1.tedawardparser
package tedExport
package formSection

final case class ContractAward(
    contractingAuthority: ContractingAuthority,
    objectOfContract: ObjectContract,
    awardContracts: List[AwardContract],
    complementaryInformaiton: ComplementaryInformation
)
