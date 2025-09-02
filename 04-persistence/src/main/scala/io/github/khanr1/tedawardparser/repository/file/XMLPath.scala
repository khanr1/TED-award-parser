package io.github.khanr1.tedawardparser
package repository
package file

import cats.Show
import cats.syntax.all.*
import scala.xml.XML

//Qualified Name
final case class QName(prefix: Option[String], local: String):
  override def toString(): String = prefix.fold(local)(p => s"$p:$local")

object QName:
  def apply(local: String): QName = QName(None, local)
  def apply(prefix: String, local: String): QName = QName(Some(prefix), local)
  given Show[QName] = Show.fromToString

// A step in a XML file element lookup or attribute lookup (on the last element). */
enum Segment:
  case Elem(name: QName)
  case Attr(name: QName)
  case WhereAttrEquals(attr: QName, value: String)
  case Index(i: Int)

opaque type XMLPath = Vector[Segment]

object XMLPath:

  def apply(first: String, rest: String*): XMLPath =
    Segment.Elem(QName(first)) +: rest.toVector.map(s => Segment.Elem(QName(s)))

  /** Parse simple strings like "a/b/c" or "a/b/@id" or "ns:a/ns:b/@ns:id". */
  def parse(s: String): XMLPath = {
    val parts: Vector[String] = s.split('/').toVector.filter(x => x.nonEmpty)
    val segmemts = parts.map { s =>
      s match
        case a if a.startsWith("@") => {
          val raw = a.drop(1)
          raw.split(':') match
            case Array(p, l) => Segment.Attr(QName(Some(p), l))
            case Array(l)    => Segment.Attr(QName(l))
        }
        case e =>
          e.split(':') match
            case Array(p, l) => Segment.Elem(QName(Some(p), l))
            case Array(l)    => Segment.Elem(QName(l))

    }
    segmemts
  }

  given Show[XMLPath] = Show.show[XMLPath](p =>
    p.map {
      case Segment.Elem(q)               => q.show
      case Segment.Attr(q)               => s"@${q.show}"
      case Segment.Index(i)              => s"[$i]"
      case Segment.WhereAttrEquals(q, v) => s"[${q.show}='$v']"
    }.mkString("/")
  )

  extension (p: XMLPath)

    def segments: Vector[Segment] = p
    infix def /(child: String): XMLPath = p :+ Segment.Elem(QName(child))
    infix def /(child: QName): XMLPath = p :+ Segment.Elem(child)
    infix def attr(attr: String): XMLPath = p :+ Segment.Attr(QName(attr))
    infix def attr(attr: QName): XMLPath = p :+ Segment.Attr(attr)
    infix def idx(i: Int): XMLPath = p :+ Segment.Index(i)
    infix def whereAttr(name: String, value: String): XMLPath =
      p :+ Segment.WhereAttrEquals(QName(name), value)
    infix def whereAttr(name: QName, value: String): XMLPath =
      p :+ Segment.WhereAttrEquals(name, value)

    /** Concatenate two paths. */
    def ++(that: XMLPath): XMLPath = p ++ that
