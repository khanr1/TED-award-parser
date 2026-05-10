package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

enum OfficialJournalSeries:
    case L, S, C

object OfficialJournalSeries:
    val toStringMapping: Map[OfficialJournalSeries, String] = OfficialJournalSeries.values.map(x => (x,x.toString)).toMap
    val fromStringMapping = toStringMapping.map((k,v)=> (v,k))

    def apply(s:String):Either[Throwable,OfficialJournalSeries]= Either.catchNonFatal(fromStringMapping(s))

    given Show[OfficialJournalSeries] = Show.show(x => toStringMapping(x) )
    given Eq[OfficialJournalSeries] = Eq.fromUniversalEquals
