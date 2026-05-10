package io.github.khanr1.tedawardparser.tedExport.formSection

import io.github.khanr1.tedawardparser.tedExport.types.*

final case class ComplementaryInformation(
    additionalInformation: Option[AdditionalInformation],
    relatesToEuProject: Option[EuProjectReference],
    noticeDispatchDate: Option[DispatchDate],
    appealBodyName: Option[Name],
    justification: Option[Justification]
)
