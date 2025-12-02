package io.github.khanr1.tedawardparser
package tedExport
package formSection

final case class ContractAward(
    contractingAuthority: ContractingAuthorityPath,
    objectOfContract: ObjectContract,
    procedure: Procedure,
    awardContracts: List[AwardContract],
    complementaryInformaiton: ComplementaryInformatio
)
