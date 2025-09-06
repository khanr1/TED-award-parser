package io.github.khanr1.tedawardparser
package repository
package parsers

import scala.xml.{Elem, Node, NodeSeq}
import cats.syntax.all.*
import scala.util.CommandLineParser.ParseError
import parsers.ParserError
//import xmlPath.{QName, Segment, XMLPath}
import Matching.*

/** Namespace bindings for resolution. Use when your XML uses prefixes. */
final case class Ns(prefixToUri: Map[String, String]):
  def uriOf(prefix: Option[String]): Option[String] =
    prefix.flatMap(prefixToUri.get)

object Ns:
  val empty: Ns = Ns(Map.empty)

private object Matching:
  /** Match element by local + optional namespace URI from Ns. */
  def elemMatches(el: Elem, q: QName, ns: Ns): Boolean =
    val localOk = el.label == q.local
    q.prefix match
      case None => localOk
      case Some(pfx) =>
        val want = ns.prefixToUri.getOrElse(pfx, "")
        localOk && Option(el.namespace).contains(want)

  /** Try attribute by "prefix:local" or "local" name. */
  def attrValue(n: Node, q: QName, ns: Ns): Option[String] =
    val key = q.prefix.fold(q.local)(p => s"$p:${q.local}")
    n.attribute(key)
      .map(_.text.trim)
      .orElse(
        n.attribute(q.local).map(_.text.trim)
      ) // fallback when stored unqualified

/** Utilities and extension methods for navigating XML using the lightweight
  * `XMLPath` DSL defined in `XMLPath.scala`.
  *
  * This module provides XPath-like helpers over Scala XML (`scala.xml.Elem`),
  * with first-class support for namespaces, attributes, basic predicates, and
  * convenient fallbacks. It is intentionally small and predictable so that
  * parsing logic remains explicit and testable.
  *
  * Key concepts
  *   - `XMLPath`: a vector of `Segment` steps (elements, attribute at the end,
  *     positional index, and a simple attribute-equals predicate). Build
  *     complex paths via the DSL (e.g., `p.idx(0)`, `p.whereAttr("id", "x")`).
  *     Note: `XMLPath.parse` supports only element/attribute segments (with
  *     optional namespace prefixes), not indices or predicates. Examples:
  *     - `XMLPath.parse("a/b/c")` selects nested elements `a -> b -> c`.
  *     - `XMLPath.parse("a/b/@id")` selects the attribute `id` on element `b`.
  *     - `XMLPath("a") / "b" / QName("ns", "c")` is namespace-aware.
  *   - `Ns`: a mapping from XML prefixes to namespace URIs used to resolve
  *     qualified names during matching. If you do not use prefixes, pass
  *     `Ns.empty`.
  *
  * Common operations
  *   - `nodesAt(path)`: resolve the element portion of a path to matching
  *     nodes.
  *   - `textAt(path)`: read normalized text of the first matching node.
  *   - `attrAt(path)`: read an attribute when the path ends with `@attr`.
  *   - `firstText(paths)` / `firstAttr(paths)`: try alternatives until one
  *     succeeds.
  *   - `childrenAtWithFallback(primary, fallback)`: use fallback roots only
  *     when primary roots yield no children.
  *   - `extractManyWithFallback(...)` and `extractManyAttrWithFallback(...)`:
  *     batch extraction helpers that also return `ParserError` on missing
  *     fields.
  *
  * Matching semantics and caveats
  *   - Element matching uses local name and optional namespace URI via `Ns`.
  *   - Attribute lookup tries both qualified (`prefix:local`) and unqualified
  *     (`local`) forms to accommodate varying serializers.
  *   - `WhereAttrEquals(name, value)` filters child elements by an attribute’s
  *     exact string value.
  *   - `Index(i)` selects the i-th matching sibling (0-based) among candidates.
  *   - `textAt` collapses whitespace (`\s+` -> single space) and trims.
  *   - Error-returning variants surface `ParserError.MissingField` with the
  *     attempted path(s) for easier diagnostics.
  *
  * Example
  * {{{
  *   import io.github.khanr1.tedawardparser.repository.parsers.*
  *   val xml: scala.xml.Elem = // ...
  *   val ns  = Ns(Map("ns" -> "urn:example"))
  *   val idP = XMLPath.parse("root/ns:item/@id")
  *   val t1  = XMLPath.parse("root/item/title")
  *   val t2  = XMLPath.parse("root/altTitle")
  *
  *   val id: Option[String]     = xml.attrAt(idP, ns)
  *   val title: Option[String]  = xml.firstText(List(t1, t2), ns)
  * }}}
  */
object XMLPathUtils:

  extension (p: XMLPath)
    /** True if the path ends with an attribute segment.
      *
      * Example:
      * {{{
      * val p1 = XMLPath.parse("a/b")
      * val p2 = XMLPath.parse("a/b/@id")
      * p1.isAttrPath // false
      * p2.isAttrPath // true
      * }}}
      */
    def isAttrPath: Boolean =
      p.segments.lastOption.exists(_.isInstanceOf[Segment.Attr])

    /** The element-only prefix of the path (i.e., strip trailing @attr if
      * present).
      *
      * Example:
      * {{{
      * val p  = XMLPath.parse("a/b/@id")
      * val es = p.elementPrefix // Vector(Elem(QName(None,"a")), Elem(QName(None,"b")))
      * }}}
      */
    def elementPrefix: Vector[Segment] =
      if isAttrPath then p.segments.dropRight(1) else p.segments

    /** The attribute qualified-name when the path ends with `@attr`.
      *
      * Example:
      * {{{
      * XMLPath.parse("a/b/@id").attrQName.map(_.local) // Some("id")
      * XMLPath.parse("a/b").attrQName                 // None
      * }}}
      */
    def attrQName: Option[QName] =
      p.segments.lastOption.collect { case Segment.Attr(q) => q }

  extension (e: Elem)
    /** Resolve element steps of a path to nodes (attributes ignored here).
      *
      * Example:
      * {{{
      * val xml = <root><a><b id="1">x</b></a></root>
      * xml.nodesAt(XMLPath.parse("root/a/b")) // NodeSeq(<b id="1">x</b>)
      * }}}
      */
    def nodesAt(path: XMLPath, ns: Ns = Ns.empty): NodeSeq = {
      // 1) Detect if the *last* segment is an attribute; if so, we'll handle it after walking
      val lastAttr: Option[QName] = path.segments.lastOption.collect {
        case Segment.Attr(value) => value
      }
      // 2) We'll walk all segments except a trailing Attr (if present)
      val walkSegments: Vector[Segment] =
        path.segments.dropRight(if lastAttr.isDefined then 1 else 0)
      // 3) Walk the DOM starting with the current element as the only candidate
      val walked: NodeSeq =
        walkSegments.foldLeft(NodeSeq.fromSeq(Seq(e: Node))) {
          case (candidates, Segment.Elem(q)) =>
            candidates.flatMap(n =>
              n.child.collect {
                case el: Elem if elemMatches(el, q, ns) => el
              }
            )

          case (candidates, Segment.WhereAttrEquals(q, v)) =>
            candidates.flatMap(n =>
              n.child.collect {
                case el: Elem if Matching.attrValue(el, q, ns).contains(v) => el
              }
            )
          case (candidates, Segment.Index(i)) =>
            // keep only the i-th candidate element (0-based), if any
            val elems = candidates.collect { case el: Elem => el }
            if i >= 0 && i < elems.length then NodeSeq.fromSeq(Seq(elems(i)))
            else NodeSeq.Empty

          case (candidates, _: Segment.Attr) =>
            // Attr at non-terminal position is ignored here; terminal Attr is handled below
            candidates
        }

      lastAttr match
        case None    => walked
        case Some(_) => walked.collect { case el: Elem => el }

    }

    /** Read an attribute off the current element (no path walking).
      *
      * Example:
      * {{{
      * val xml = <a id="42"/>
      * xml.rootAttr("id") // Some("42")
      * }}}
      */
    def rootAttr(name: String): Option[String] =
      e.attribute(name).map(_.text.trim).filter(_.nonEmpty)

    /** First node’s normalized text at an element path. Collapses whitespace.
      *
      * Example:
      * {{{
      * val xml = <root><title>  Hello\nWorld </title></root>
      * xml.textAt(XMLPath.parse("root/title")) // Some("Hello World")
      * }}}
      */
    def textAt(path: XMLPath, ns: Ns = Ns.empty): Option[String] =
      nodesAt(path, ns).headOption
        .map(_.text.replaceAll("\\s+", " ").trim)
        .filter(_.nonEmpty)

    /** First node’s normalized text at element path or `ParserError`.
      *
      * Example:
      * {{{
      * val xml = <root/>
      * xml.textAtOrError(XMLPath.parse("root/title"), field = "title")
      * // Left(ParserError.MissingField("title", Some("root/title")))
      * }}}
      */
    def textAtOrError(
        path: XMLPath,
        field: String,
        ns: Ns = Ns.empty
    ): Either[ParserError, String] =
      textAt(path, ns).toRight(ParserError.MissingField(field, Some(path.show)))

    /** For each parent path in `validPath`, collect children and read text at
      * `itemTag` under each.
      *
      * Example:
      * {{{
      * val xml = <root><items><i>a</i><i>b</i></items></root>
      * val parents = List(XMLPath.parse("root/items"))
      * val itemTag = XMLPath.parse("i")
      * xml.allTextAt(parents, itemTag) // List(Some("a"), Some("b"))
      * }}}
      */
    def allTextAt(
        validPath: List[XMLPath],
        itemTag: XMLPath
    ): List[Option[String]] =
      val children = validPath.flatMap(p => e.childrenAt(p))
      children.map(elem => elem.textAt(itemTag))

    /** Like `allTextAt`, but each missing value becomes a `ParserError`.
      *
      * Example:
      * {{{
      * val xml = <root><items><i>a</i><i/></items></root>
      * val parents = List(XMLPath.parse("root/items"))
      * val itemTag = XMLPath.parse("i")
      * xml.allTextAtOrError(parents, itemTag, field = "item")
      * // List(Right("a"), Left(MissingField("item", Some("root/items"))))
      * }}}
      */
    def allTextAtOrError(
        validPath: List[XMLPath],
        itemTag: XMLPath,
        field: String
    ): List[Either[ParserError, String]] =
      allTextAt(validPath, itemTag).map(option =>
        option.toRight(
          ParserError.MissingField(field, Some(validPath.showAltPath()))
        )
      )

    /** Attribute value when path ends with `@attr`.
      *
      * Example:
      * {{{
      * val xml = <root><a id="7"/></root>
      * xml.attrAt(XMLPath.parse("root/a/@id")) // Some("7")
      * }}}
      */
    def attrAt(path: XMLPath, ns: Ns = Ns.empty): Option[String] =
      path.attrQName.flatMap { q =>
        nodesAt(path, ns).headOption.flatMap(n => Matching.attrValue(n, q, ns))
      }

    /** Children elements at the element path.
      *
      * Example:
      * {{{
      * val xml = <root><a><b/><b/></a></root>
      * xml.childrenAt(XMLPath.parse("root/a")) // List(<b/>, <b/>)
      * }}}
      */
    def childrenAt(path: XMLPath, ns: Ns = Ns.empty): List[Elem] =
      nodesAt(path, ns).collect { case el: Elem => el }.toList

    /** All child elements directly under any of the `under` paths.
      *
      * Example:
      * {{{
      * val xml = <r><xs><x/><x/></xs><ys><y/></ys></r>
      * xml.selectUnder(List(XMLPath.parse("r/xs"), XMLPath.parse("r/ys")))
      * // List(<x/>, <x/>, <y/>)
      * }}}
      */
    def selectUnder(under: List[XMLPath], ns: Ns = Ns.empty): List[Elem] =
      under.flatMap(p => e.childrenAt(p, ns))

    /** First successful text among alternative paths.
      *
      * Example:
      * {{{
      * val xml = <r><title2>T</title2></r>
      * val t1 = XMLPath.parse("r/title")
      * val t2 = XMLPath.parse("r/title2")
      * xml.firstText(List(t1, t2)) // Some("T")
      * }}}
      */
    def firstText(paths: List[XMLPath], ns: Ns = Ns.empty): Option[String] =
      paths.iterator.map(textAt(_, ns)).collectFirst { case Some(v) => v }

    /** First successful text among alternative paths or `ParserError`.
      *
      * Example:
      * {{{
      * val xml = <r/>
      * xml.firstTextOrError(List(XMLPath.parse("r/title")), "title")
      * // Left(MissingField("title", Some("r/title")))
      * }}}
      */
    def firstTextOrError(
        paths: List[XMLPath],
        field: String,
        ns: Ns = Ns.empty
    ): Either[ParserError, String] =
      firstText(paths, ns).toRight(
        ParserError.MissingField(field, Some(paths.showAltPath()))
      )

    /** First successful attribute among alternative `@attr` paths.
      *
      * Example:
      * {{{
      * val xml = <r><a id="1"/></r>
      * xml.firstAttr(List(XMLPath.parse("r/a/@id"))) // Some("1")
      * }}}
      */
    def firstAttr(paths: List[XMLPath], ns: Ns = Ns.empty): Option[String] =
      paths.iterator.map(attrAt(_, ns)).collectFirst { case Some(v) => v }

    /** First successful attribute among alternative `@attr` paths, else error.
      *
      * Example:
      * {{{
      * val xml = <r/>
      * xml.firstAttrOrError(List(XMLPath.parse("r/a/@id")), "id")
      * // Left(MissingField("id", Some("r/a/@id")))
      * }}}
      */
    def firstAttrOrError(
        paths: List[XMLPath],
        field: String,
        ns: Ns = Ns.empty
    ): Either[ParserError, String] =
      firstAttr(paths, ns).toRight(
        ParserError.MissingField(field, Some(paths.showAltPath()))
      )

    /** Collect child elements for each path, concatenated.
      *
      * Example:
      * {{{
      * val xml = <r><xs><x/></xs><ys><y/></ys></r>
      * xml.childrenAtAll(List(XMLPath.parse("r/xs"), XMLPath.parse("r/ys")))
      * // List(<x/>, <y/>)
      * }}}
      */
    def childrenAtAll(paths: List[XMLPath], ns: Ns = Ns.empty): List[Elem] =
      paths.flatMap(e.childrenAt(_, ns))

    /** Try primary roots; if empty, try fallback roots (ONLY fallback).
      *
      * Example:
      * {{{
      * val xml = <r><fallback><i/></fallback></r>
      * val prim = List(XMLPath.parse("r/primary"))
      * val fb   = List(XMLPath.parse("r/fallback"))
      * xml.childrenAtWithFallback(prim, fb) // List(<i/>)
      * }}}
      */
    def childrenAtWithFallback(
        primary: List[XMLPath],
        fallback: List[XMLPath],
        ns: Ns = Ns.empty
    ): List[Elem] =
      val prim = e.childrenAtAll(primary, ns)
      if prim.nonEmpty then prim else e.childrenAtAll(fallback, ns)

    /** Extract many text values from item paths under parent roots, with
      * fallback between primary and fallback root sets.
      *
      * Example:
      * {{{
      * val xml = <r><fb><t>a</t><t>b</t></fb></r>
      * val primRoots = List(XMLPath.parse("r/prim"))
      * val fbRoots   = List(XMLPath.parse("r/fb"))
      * val item      = XMLPath.parse("t")
      * xml.extractManyWithFallback(primRoots, fbRoots, item, item, "t")
      * // List(Right("a"), Right("b"))
      * }}}
      */
    def extractManyWithFallback(
        primaryRoots: List[XMLPath],
        fallbackRoots: List[XMLPath],
        primaryItem: XMLPath, // e.g. ContractValue.PrimaryValueTotal or Title
        fallbackItem: XMLPath, // e.g. ContractValue.FallbackValueTotal or Title
        field: String, // for MissingField
        ns: Ns = Ns.empty
    ): List[Either[ParserError, String]] =
      val primParents = e.childrenAtAll(primaryRoots, ns)
      val prim = primParents.getTextsAt(
        primaryRoots,
        item = primaryItem,
        field = field,
        ns = ns
      )
      if prim.exists(_.isRight) || fallbackRoots.isEmpty then prim
      else
        val fbParents = e.childrenAtAll(fallbackRoots, ns)
        fbParents.getTextsAt(
          fallbackRoots,
          item = fallbackItem,
          field = field,
          ns = ns
        )

    /** Extract many attribute values under parent roots with fallback. The
      * `item` path should end with `@attr`. The `attrName` parameter is
      * informational; attribute selection is determined by `item`.
      *
      * Example:
      * {{{
      * val xml = <r><fb><a id="1"/><a id="2"/></fb></r>
      * val primRoots = List(XMLPath.parse("r/prim"))
      * val fbRoots   = List(XMLPath.parse("r/fb"))
      * val item      = XMLPath.parse("a/@id")
      * xml.extractManyAttrWithFallback(primRoots, fbRoots, item, item, "id", "id")
      * // List(Right("1"), Right("2"))
      * }}}
      */
    def extractManyAttrWithFallback(
        primaryRoots: List[XMLPath],
        fallbackRoots: List[XMLPath],
        primaryItem: XMLPath, // base path that owns the attr
        fallbackItem: XMLPath, // base path that owns the attr
        attrName: String, // attribute name for both branches
        field: String, // for MissingField
        ns: Ns = Ns.empty
    ): List[Either[ParserError, String]] =
      val primParents = e.childrenAtAll(primaryRoots, ns)
      val prim = primParents.getAttrsAt(
        primaryRoots,
        item = primaryItem,
        field = field,
        ns = ns
      )
      if prim.exists(_.isRight) || fallbackRoots.isEmpty then prim
      else
        val fbParents = e.childrenAtAll(fallbackRoots, ns)
        fbParents.getAttrsAt(
          fallbackRoots,
          item = fallbackItem,
          field = field,
          ns = ns
        )

  extension (l: List[Elem])
    /** For each parent element in the list, read text at `item`. Missing values
      * produce `ParserError.MissingField` with the attempted paths for easier
      * diagnostics.
      *
      * Example:
      * {{{
      * val parents = List(<p><t>a</t></p>, <p/>)
      * val item    = XMLPath.parse("t")
      * parents.getTextsAt(List(XMLPath.parse("root")), item, "t")
      * // List(Right("a"), Left(MissingField("t", Some("root"))))
      * }}}
      */
    def getTextsAt(
        paths: List[XMLPath],
        item: XMLPath,
        field: String,
        ns: Ns = Ns.empty
    ): List[Either[ParserError, String]] =
      l.map(e =>
        e.textAt(item, ns)
          .toRight(ParserError.MissingField(field, Some(paths.showAltPath())))
      )

    /** For each parent element in the list, read attribute at `item` (must end
      * with `@attr`). Missing values produce a `ParserError`.
      *
      * Example:
      * {{{
      * val parents = List(<p id="1"/>, <p/>)
      * val item    = XMLPath.parse("@id")
      * parents.getAttrsAt(List(XMLPath.parse("root")), item, "id")
      * // List(Right("1"), Left(MissingField("id", Some("root"))))
      * }}}
      */
    def getAttrsAt(
        paths: List[XMLPath],
        item: XMLPath,
        field: String,
        ns: Ns = Ns.empty
    ): List[Either[ParserError, String]] =
      l.map(e =>
        e.attrAt(item, ns)
          .toRight(ParserError.MissingField(field, Some(paths.showAltPath())))
      )

  extension (paths: List[XMLPath])
    /** Show alternative paths as a single string (newline by default).
      *
      * Example:
      * {{{
      * List(XMLPath.parse("a/b"), XMLPath.parse("x/y")).showAltPath()
      * // "a/b\nx/y"
      * }}}
      */
    def showAltPath(sep: String = "\n") = paths.map(p => p.show).mkString(sep)
