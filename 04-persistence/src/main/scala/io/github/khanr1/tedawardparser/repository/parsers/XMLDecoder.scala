package io.github.khanr1.tedawardparser
package repository
package parsers

import scala.xml.Elem
import cats.Monad
import cats.syntax.all.*

trait XMLDecoder[A]:
  def decode(e: Elem): Either[ParserError, A]
