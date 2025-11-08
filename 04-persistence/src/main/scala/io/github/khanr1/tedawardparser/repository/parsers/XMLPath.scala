package io.github.khanr1.tedawardparser
package repository
package parsers

import cats.Show
import cats.syntax.all.*
import scala.xml.XML

/** Qualified XML name consisting of an optional prefix and a local part.
  *
  * What it represents
  *   - `QName("id")` → unqualified name `id`.
  *   - `QName("ns", "item")` → qualified name `ns:item`.
  *
  * Rendering
  *   - `toString` (and `Show[QName]`) renders `prefix:local` when a prefix is
  *     present, otherwise just the `local` name.
  *
  * Usage
  *   - Use unqualified names for XML without namespaces or when matching by
  *     local name only.
  *   - Use a prefixed `QName` when you want matching tied to a specific
  *     namespace URI (resolution happens in XML navigation helpers such as
  *     `XMLPathUtils.nodesAt(..., ns)` that consult the `Ns` mapping).
  *
  * Examples
  * {{{
  * QName("id").toString            // "id"
  * QName("cbc", "Title").toString  // "cbc:Title"
  * }}}
  */
final case class QName(prefix: Option[String], local: String):
  override def toString(): String = prefix.fold(local)(p => s"$p:$local")

object QName:
  def apply(local: String): QName = QName(None, local)
  def apply(prefix: String, local: String): QName = QName(Some(prefix), local)
  given Show[QName] = Show.fromToString

/** A step in an XML path used to navigate elements and attributes.
  *
  * Cases
  *   - `Elem(name)`: descend to a child element with the qualified name.
  *   - `Attr(name)`: select an attribute on the current element (should be the
  *     trailing step when used for value lookup).
  *   - `WhereAttrEquals(attr, value)`: filter the current candidate elements
  *     where the attribute equals the given `value` (XPath-style predicate).
  *   - `Index(i)`: select the i-th candidate among siblings (0-based).
  *
  * Examples
  * {{{
  * import io.github.khanr1.tedawardparser.repository.parsers.*
  * // Unqualified
  * val p1 = XMLPath("root") / "items" / "item"          // Elem,Elem,Elem
  * val p2 = p1.attr("id")                                 // .../@id
  * val p3 = p1.whereAttr("type", "primary").idx(0)       // ...[type='primary']/[0]
  *
  * // With namespaces (qualify using QName)
  * val p4 = (XMLPath("r") / QName("cac", "Party") / QName("cac", "Item"))
  * val p5 = (p4 / QName("cbc", "Title")).attr(QName("cbc", "lang"))
  * }}}
  */
enum Segment:
  case Elem(name: QName)
  case Attr(name: QName)
  case WhereAttrEquals(attr: QName, value: String)
  case Index(i: Int)

/** An XPath-like, minimal path representation built from `Segment`s.
  *
  * Build paths using the DSL-style combinators here, and use
  * `XMLPathUtils` to evaluate paths against `scala.xml` trees.
  *
  * Notes
  *   - `XMLPath.parse("...")` parses simple element/attribute segments only.
  *     Add indices and attribute predicates using the DSL (`idx`, `whereAttr`).
  *   - Namespace prefixes on segments are represented via `QName(prefix,local)`
  *     or inline in strings parsed by `parse` (e.g. `cbc:Title`). Resolution of
  *     prefixes to namespace URIs is handled by `XMLPathUtils` methods via an
  *     `Ns` mapping.
  *
  * Examples (without namespaces)
  * {{{
  * import io.github.khanr1.tedawardparser.repository.parsers.*
  * val p  = XMLPath("root", "items", "item")             // root/items/item
  * val pA = p.attr("id")                                   // root/items/item/@id
  * val pT = (p.idx(0) / "title")                          // root/items/item/[0]/title
  * val pF = (p.whereAttr("type", "primary") / "title")   // root/items/item[type='primary']/title
  * }}}
  *
  * Examples (with namespaces)
  * {{{
  * import io.github.khanr1.tedawardparser.repository.parsers.*
  * val item = XMLPath("r") / QName("cac", "Party") / QName("cac", "Item")
  * val titl = item / QName("cbc", "Title")
  * val lang = titl.attr(QName("cbc", "languageID"))        // r/cac:Party/cac:Item/cbc:Title/@cbc:languageID
  * val filt = item.whereAttr(QName("cbc", "id"), "X")     // r/cac:Party/cac:Item[cbc:id='X']
  * }}}
  */
opaque type XMLPath = Vector[Segment]

object XMLPath:
  def splitQName(s: String): (Option[String], String) =
    s.indexOf(':') match
      case -1 => (None, s)
      case i  => (Some(s.substring(0, i)), s.substring(i + 1))

  /** Construct a simple element path from a head element and 0+ child names.
    *
    * Examples (no namespaces):
    * {{{
    * val p: XMLPath = XMLPath("root", "a", "b") // root/a/b
    * }}}
    *
    * Examples (with namespaces via `QName`):
    * {{{
    * val p = (XMLPath("r") / QName("ns", "A") / QName("ns", "B")) // r/ns:A/ns:B
    * }}}
    */
  def apply(first: String, rest: String*): XMLPath =
    val head = splitQName(first)
    val tail = rest.toVector.map(x => splitQName(x))
    Segment.Elem(QName(head._1, head._2)) +: tail.map(s =>
      Segment.Elem(QName(s._1, s._2))
    )

  /** Parse simple strings like "a/b/c", "a/b/@id", or "ns:a/ns:b/@ns:id".
    *
    * What it supports
    *   - Element and attribute segments, optionally prefixed (e.g. `cbc:Title`,
    *     `@cbc:id`).
    *   - Does NOT parse indices or predicates; add those via the DSL methods
    *     (`idx`, `whereAttr`).
    *
    * Examples
    * {{{
    * XMLPath.parse("root/items/item")              // Elem segments only
    * XMLPath.parse("root/items/item/@id")          // Trailing attribute
    * XMLPath.parse("r/cac:Party/cac:Item/@cbc:id") // Namespaced segments
    * }}}
    */
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
      * Examples:
      * {{{
      * XMLPath("root") / "a" / "b"              // root/a/b
      * }}}
      */
    infix def /(child: String): XMLPath = p :+ Segment.Elem(QName(child))

    /** Append an element by qualified name.
      *
      * Examples (with namespaces):
      * {{{
      * XMLPath("r") / QName("cac", "Party") / QName("cac", "Item")
      * // r/cac:Party/cac:Item
      * }}}
      */
    infix def /(child: QName): XMLPath = p :+ Segment.Elem(child)

    /** Append an attribute segment (to be used as the final step).
      *
      * Examples:
      * {{{
      * XMLPath("a", "b").attr("id")                     // a/b/@id
      * (XMLPath("r") / QName("cbc", "Title")).attr(QName("cbc", "lang"))
      * // r/cbc:Title/@cbc:lang
      * }}}
      */
    infix def attr(attr: String): XMLPath = p :+ Segment.Attr(QName(attr))

    /** Append an attribute segment using a qualified name. */
    infix def attr(attr: QName): XMLPath = p :+ Segment.Attr(attr)

    /** Select the i-th matching sibling (0-based) among current candidates.
      *
      * Examples:
      * {{{
      * (XMLPath("root") / "items" / "item").idx(0)     // root/items/item/[0]
      * }}}
      */
    infix def idx(i: Int): XMLPath = p :+ Segment.Index(i)

    /** Filter the current candidate elements where `name == value` on the attribute
      * (XPath-style predicate applied to the preceding step).
      *
      * Examples:
      * {{{
      * XMLPath("items").whereAttr("type", "primary")        // items[type='primary']
      * (XMLPath("r") / QName("cac", "Item")).whereAttr(QName("cbc", "id"), "X")
      * // r/cac:Item[cbc:id='X']
      * }}}
      */
    infix def whereAttr(name: String, value: String): XMLPath =
      p :+ Segment.WhereAttrEquals(QName(name), value)

    /** Filter the current candidate elements with an attribute match using a qualified name. */
    infix def whereAttr(name: QName, value: String): XMLPath =
      p :+ Segment.WhereAttrEquals(name, value)

    /** Concatenate two paths. */
    def ++(that: XMLPath): XMLPath = p ++ that
