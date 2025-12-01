package io.github.khanr1.tedawardparser
package repository
package xml

import scala.xml.Elem
import cats.Monad
import cats.syntax.all.*
import cats.data.ValidatedNel



trait XMLDecoder[A<:Raw]:
  def decode(e: Elem): A


object XMLDecoder:
  def apply[A<:Raw](using decoder: XMLDecoder[A]):XMLDecoder[A]= decoder


trait Raw 