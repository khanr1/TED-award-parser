package io.github.khanr1
package tedawardparser

import cats.effect.{IO, IOApp}
import fs2.io.file.{Files, Path}
import fs2.text
import scala.xml.XML
import scala.xml.Elem

object Main extends IOApp.Simple {

  val dir = Path(
    "/Users/raphaelkhan/Developer/ted-award-parser/04-persistence/src/main/resources/TED_08-11-2025"
  )

  val run = IO.println("Hello, World!")

}
