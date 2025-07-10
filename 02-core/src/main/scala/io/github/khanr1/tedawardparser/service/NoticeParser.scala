package io.github.khanr1.tedawardparser
package service

trait NoticeParser[F[_], A] {
  def parseStream(xmls: fs2.Stream[F, A]): fs2.Stream[F, Notice]
}
