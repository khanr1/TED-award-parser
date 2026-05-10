package io.github.khanr1.tedawardparser
package repository
package xml

import scala.xml.Elem

/** Typeclass for decoding a `scala.xml.Elem` into a raw data holder `A`.
  *
  * Implementations live in the `decoders` sub-packages. Obtain instances via
  * `XMLDecoder[A]` (summoner) or through context-bound / `given` imports.
  *
  * @tparam A the target raw type, must extend [[Raw]]
  */
trait XMLDecoder[A <: Raw]:
  def decode(e: Elem): A

object XMLDecoder:
  /** Summoner: fetch the implicit `XMLDecoder[A]` in scope. */
  def apply[A <: Raw](using decoder: XMLDecoder[A]): XMLDecoder[A] = decoder

/** Marker trait for raw data holders produced by [[XMLDecoder]] implementations.
  *
  * Raw types sit between the XML source and the domain model. Their fields are
  * typed as `Either[ParserError, T]` so that decoding failures are accumulated
  * and surfaced explicitly rather than thrown.
  */
trait Raw
