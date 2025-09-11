package io.github.khanr1.tedawardparser
package repository
package parsers

import cats.Show
import cats.syntax.all.*
import scala.xml.XML

/**
  * Qualified XML name consisting of an optional prefix and a local part.
  *
  * Examples
  * - `QName("id")` represents the unqualified name `id`.
  * - `QName("ns", "item")` represents the qualified name `ns:item`.
  *
  * `toString` renders `prefix:local` when a prefix is present, otherwise just
  * the `local` name.
  */
final case class QName(prefix: Option[String], local: String):
  override def toString(): String = prefix.fold(local)(p => s"$p:$local")

object QName:
  def apply(local: String): QName = QName(None, local)
  def apply(prefix: String, local: String): QName = QName(Some(prefix), local)
  given Show[QName] = Show.fromToString

/**
  * A step in an XML path used to navigate elements and attributes.
  *
  * Cases
  * - `Elem(name)`: descend to a child element with the given qualified name.
  * - `Attr(name)`: select an attribute on the current element (must be the
  *   trailing step when used for lookup).
  * - `WhereAttrEquals(attr, value)`: filter child elements where `attr == value`.
  * - `Index(i)`: select the i-th candidate among siblings (0-based).
  */
enum Segment:
  case Elem(name: QName)
  case Attr(name: QName)
  case WhereAttrEquals(attr: QName, value: String)
  case Index(i: Int)

/**
  * An XPath-like, minimal path representation built from `Segment`s.
  *
  * Use the combinators in `object XMLPath` to construct paths fluently and
  * portably across parser implementations.
  */
opaque type XMLPath = Vector[Segment]

object XMLPath:
  /**
    * Construct a simple element path from a head element and 0+ child names.
    *
    * Example:
    * {{{
    * val p: XMLPath = XMLPath("root", "a", "b") // root/a/b
    * }}}
    */
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
    /** The underlying path segments. */
    def segments: Vector[Segment] = p
    /** Append an element by local name.
      *
      * Example: `XMLPath("root") / "a" / "b"` -> `root/a/b`
      */
    infix def /(child: String): XMLPath = p :+ Segment.Elem(QName(child))
    /** Append an element by qualified name. */
    infix def /(child: QName): XMLPath = p :+ Segment.Elem(child)
    /** Append an attribute segment (to be used as the final step).
      *
      * Example: `XMLPath("a", "b").attr("id")` -> `a/b/@id`
      */
    infix def attr(attr: String): XMLPath = p :+ Segment.Attr(QName(attr))
    /** Append an attribute segment using a qualified name. */
    infix def attr(attr: QName): XMLPath = p :+ Segment.Attr(attr)
    /** Select the i-th matching sibling (0-based) among current candidates. */
    infix def idx(i: Int): XMLPath = p :+ Segment.Index(i)
    /** Filter child elements where `name == value` on the attribute.
      *
      * Example: `XMLPath("items") .whereAttr("type", "primary")`
      */
    infix def whereAttr(name: String, value: String): XMLPath =
      p :+ Segment.WhereAttrEquals(QName(name), value)
    /** Filter child elements with an attribute match using a qualified name. */
    infix def whereAttr(name: QName, value: String): XMLPath =
      p :+ Segment.WhereAttrEquals(name, value)

    /** Concatenate two paths. */
    def ++(that: XMLPath): XMLPath = p ++ that
