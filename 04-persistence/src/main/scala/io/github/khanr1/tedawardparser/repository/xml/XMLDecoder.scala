package io.github.khanr1.tedawardparser
package repository
package xml

import scala.xml.Elem
import cats.Monad
import cats.syntax.all.*


trait XMLDecoder[A]:
  def decode(e: Elem,path: XMLPath): Either[ParserError,A]


object XMLDecoder:
  def apply[A](using decoder: XMLDecoder[A]):XMLDecoder[A]= decoder