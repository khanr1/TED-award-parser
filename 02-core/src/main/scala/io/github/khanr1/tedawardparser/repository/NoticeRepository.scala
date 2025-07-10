package io.github.khanr1.tedawardparser.repository

trait NoticeRepository[F[_]] {
  // stream all XMl element
  def streamXMLElements: fs2.Stream[F, xml.Elem]
}
