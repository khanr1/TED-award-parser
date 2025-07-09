package io.github.khanr1.tedawardparser

opaque type NoticeNumber = String

object NoticeNumber:
  def apply(s: String): NoticeNumber = s
  extension (n: NoticeNumber) def value: String = n
