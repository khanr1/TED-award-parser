package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type DocumentType = String
object DocumentType:
  def apply(s: String): DocumentType = s
  extension (x: DocumentType) def value: String = x
  given Show[DocumentType] = Show.fromToString
  given Eq[DocumentType] = Eq.fromUniversalEquals
