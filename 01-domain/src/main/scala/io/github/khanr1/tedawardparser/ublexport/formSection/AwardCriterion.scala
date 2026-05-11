package io.github.khanr1.tedawardparser
package ublExport
package formSection

import io.github.khanr1.tedawardparser.tedExport.types.Description
import io.github.khanr1.tedawardparser.ublExport.types.AwardCriterionTypeCode

/** A single award criterion from cac:SubordinateAwardingCriterion.
  * UBL models criteria as structured objects (type + description + weight),
  * unlike TED which stored award criteria as a single code string.
  */
final case class AwardCriterion(
    criterionType: AwardCriterionTypeCode, // cbc:AwardingCriterionTypeCode e.g. "price","quality","cost"
    description: Option[Description],      // cbc:Description
    weightPercent: Option[Int]             // efbc:ParameterNumeric (percentage weight)
)
