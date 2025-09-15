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

  /** Try attribute by qualified name or by namespace URI + local name.
    *
    * Matching order:
    *   1) Direct lookup using the textual qualified key (e.g., "cbc:id") when a prefix is present.
    *   2) Namespace-aware scan: resolve the desired URI from `Ns` and scan attributes,
    *      comparing each attribute's resolved URI and local name.
    *   3) Fallback to unqualified local name (common for attributes serialized without a namespace).
    */
  def attrValue(n: Node, q: QName, ns: Ns): Option[String] =
    // 1) Direct key lookup (prefix:local)
    val direct: Option[String] =
      q.prefix.map(pre => s"$pre:${q.local}").flatMap(k => n.attribute(k).map(_.text.trim))

    // 2) Namespace-URI aware scan (robust to different prefix aliases)
    val byUri: Option[String] = (q.prefix.flatMap(ns.prefixToUri.get), n) match
      case (Some(wantUri), el: Elem) =>
        def loop(md: scala.xml.MetaData): Option[String] = md match
          case null => None
          case pa: scala.xml.PrefixedAttribute =>
            val sameLocal = pa.key == q.local
            val uri = Option(el.scope).flatMap(s => Option(s.getURI(pa.pre)))
            if sameLocal && uri.contains(wantUri) then Option(pa.value.text.trim)
            else loop(pa.next)
          case ua: scala.xml.UnprefixedAttribute => loop(ua.next)
          case other => loop(other.next)
        loop(el.attributes)
      case _ => None

    // 3) Fallback to unqualified local name
    val unqualified: Option[String] = n.attribute(q.local).map(_.text.trim)

    direct.orElse(byUri).orElse(unqualified)

/** Utilities and extension methods for navigating XML using the lightweight
  * `XMLPath` DSL defined in `XMLPath.scala`.
  *
  * This module provides XPath-like helpers over Scala XML (`scala.xml.Elem`),
  * with first-class support for namespaces, attributes, indices, simple
  * attribute predicates, and convenient fallbacks. It is intentionally small
  * and predictable so that parsing logic remains explicit and testable.
  *
  * Key concepts
  *   - `XMLPath`: a vector of `Segment` steps (elements, an optional trailing
  *     attribute, positional index, and an attribute-equals predicate). Build
  *     paths via the DSL (e.g., `p.idx(0)`, `p.whereAttr("id", "x")`).
  *     Note: `XMLPath.parse` supports only element/attribute segments (with
  *     optional namespace prefixes), not indices or predicates.
  *     Examples:
  *     - `XMLPath.parse("a/b/c")` selects nested elements `a -> b -> c`.
  *     - `XMLPath.parse("a/b/@id")` selects attribute `id` on element `b`.
  *     - `XMLPath("a") / "b" / QName("ns", "c")` is namespace-aware.
  *   - `Ns`: a mapping from XML prefixes to namespace URIs used to resolve
  *     qualified names during element matching and attribute lookups. If your
  *     XML has no prefixes, pass `Ns.empty` and use unqualified names.
  *
  * Matching semantics and caveats
  *   - Elements: we match the local name, and when a prefix is present in the
  *     path, we also require that the element’s namespace URI equals the URI in
  *     the provided `Ns` mapping for that prefix.
  *   - Attributes: we try both qualified (`prefix:local`) and unqualified
  *     (`local`) forms to accommodate varying serializers.
  *   - `WhereAttrEquals(name, value)` filters the current candidate elements by
  *     exact attribute string equality; `Index(i)` selects the i-th candidate
  *     (0-based).
  *   - `textAt` collapses whitespace (`\s+` -> single space) and trims.
  *   - Error-returning variants surface `ParserError.MissingField` with the
  *     attempted path(s) for easier diagnostics.
  *   - Default namespaces: if your XML uses a default namespace (no prefix),
  *     unprefixed path segments still match by local name only. To require a
  *     specific namespace, supply a prefix in your path (e.g., `ns:Elem`) and
  *     bind that prefix to the correct URI in `Ns`.
  *
  * Examples (without namespaces)
  * {{{
  * import io.github.khanr1.tedawardparser.repository.parsers.*
  * val xml =
  *   <root>
  *     <items>
  *       <item id="1"><title>A</title></item>
  *       <item id="2"><title>B</title></item>
  *     </items>
  *     <title>Main</title>
  *   </root>
  * // Element selection
  * xml.nodesAt(XMLPath.parse("root/items/item"))            // NodeSeq(<item id="1">..., <item id="2">...)
  * // First text and alternatives
  * xml.textAt(XMLPath.parse("root/title"))                  // Some("Main")
  * xml.firstText(List(XMLPath.parse("root/alt"), XMLPath.parse("root/title"))) // Some("Main")
  * // Attribute selection
  * xml.attrAt(XMLPath.parse("root/items/item/@id"))         // Some("1") (first match)
  * // Predicates and indices via DSL (not via parse)
  * val firstItem = XMLPath("root", "items") / "item"      .idx(0)
  * val item2ById = XMLPath("root", "items") / "item"      .whereAttr("id", "2")
  * xml.textAt(firstItem / "title")                           // Some("A")
  * xml.textAt(item2ById / "title")                          // Some("B")
  * }}}
  *
  * Examples (with namespaces)
  * {{{
  * import io.github.khanr1.tedawardparser.repository.parsers.*
  * val ns = Ns(Map(
  *   "cac" -> "urn:ex:cac",
  *   "cbc" -> "urn:ex:cbc"
  * ))
  * val xml =
  *   <r xmlns:cac="urn:ex:cac" xmlns:cbc="urn:ex:cbc">
  *     <cac:Party>
  *       <cac:Item cbc:id="X"><cbc:Title>Thing</cbc:Title></cac:Item>
  *     </cac:Party>
  *   </r>
  * // Element selection and text
  * xml.nodesAt(XMLPath.parse("r/cac:Party/cac:Item"), ns)          // NodeSeq(<cac:Item ...>)
  * xml.textAt(XMLPath.parse("r/cac:Party/cac:Item/cbc:Title"), ns) // Some("Thing")
  * // Attribute selection (qualified attribute name)
  * xml.attrAt(XMLPath.parse("r/cac:Party/cac:Item/@cbc:id"), ns)   // Some("X")
  * // Predicate using a qualified attribute and QName in the DSL
  * val itemById =
  *   (XMLPath("r") / QName("cac", "Party") / QName("cac", "Item"))
  *     .whereAttr(QName("cbc", "id"), "X")
  * xml.textAt(itemById / QName("cbc", "Title"), ns)               // Some("Thing")
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
      * Examples (no namespaces):
      * {{{
      * val xml = <root><a><b id="1">x</b></a></root>
      * xml.nodesAt(XMLPath.parse("root/a/b")) // NodeSeq(<b id="1">x</b>)
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <root xmlns:ns="urn:x"><ns:a><ns:b id="1">x</ns:b></ns:a></root>
      * val ns  = Ns(Map("ns" -> "urn:x"))
      * xml.nodesAt(XMLPath.parse("root/ns:a/ns:b"), ns) // NodeSeq(<ns:b id="1">x</ns:b>)
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
            // Filter the current candidate elements by attribute equality
            candidates.collect {
              case el: Elem if Matching.attrValue(el, q, ns).contains(v) => el
            }
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
      * Examples (no namespaces):
      * {{{
      * val xml = <a id="42"/>
      * xml.rootAttr("id") // Some("42")
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <a xmlns:ns="urn:x" ns:id="42"/>
      * // Attribute lookup accepts the literal qualified key
      * xml.attribute("ns:id").map(_.text.trim) // Some("42")
      * }}}
      */
    def rootAttr(name: String): Option[String] =
      e.attribute(name).map(_.text.trim).filter(_.nonEmpty)

    /** First node’s normalized text at an element path. Collapses whitespace.
      *
      * Examples (no namespaces):
      * {{{
      * val xml = <root><title>  Hello\nWorld </title></root>
      * xml.textAt(XMLPath.parse("root/title")) // Some("Hello World")
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <r xmlns:cbc="urn:ex:cbc"><cbc:Title> Hi\nThere </cbc:Title></r>
      * val ns  = Ns(Map("cbc" -> "urn:ex:cbc"))
      * xml.textAt(XMLPath.parse("r/cbc:Title"), ns) // Some("Hi There")
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
      * Examples (no namespaces):
      * {{{
      * val xml = <root><items><i>a</i><i>b</i></items></root>
      * val parents = List(XMLPath.parse("root/items"))
      * val itemTag = XMLPath.parse("i")
      * xml.allTextAt(parents, itemTag) // List(Some("a"), Some("b"))
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <r xmlns:ns="u"><ns:items><ns:i>a</ns:i><ns:i>b</ns:i></ns:items></r>
      * val ns  = Ns(Map("ns" -> "u"))
      * val parents = List(XMLPath.parse("r/ns:items"))
      * val itemTag = XMLPath.parse("ns:i")
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
      * Examples (no namespaces):
      * {{{
      * val xml = <root><items><i>a</i><i/></items></root>
      * val parents = List(XMLPath.parse("root/items"))
      * val itemTag = XMLPath.parse("i")
      * xml.allTextAtOrError(parents, itemTag, field = "item")
      * // List(Right("a"), Left(MissingField("item", Some("root/items"))))
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <r xmlns:ns="u"><ns:items><ns:i>a</ns:i><ns:i/></ns:items></r>
      * val ns  = Ns(Map("ns" -> "u"))
      * val parents = List(XMLPath.parse("r/ns:items"))
      * val itemTag = XMLPath.parse("ns:i")
      * xml.allTextAtOrError(parents, itemTag, field = "item")
      * // List(Right("a"), Left(MissingField("item", Some("r/ns:items"))))
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
      * Examples (no namespaces):
      * {{{
      * val xml = <root><a id="7"/></root>
      * xml.attrAt(XMLPath.parse("root/a/@id")) // Some("7")
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <r xmlns:cbc="urn:ex:cbc"><a cbc:id="7"/></r>
      * val ns  = Ns(Map("cbc" -> "urn:ex:cbc"))
      * xml.attrAt(XMLPath.parse("r/a/@cbc:id"), ns) // Some("7")
      * }}}
      */
    def attrAt(path: XMLPath, ns: Ns = Ns.empty): Option[String] =
      path.attrQName.flatMap { q =>
        nodesAt(path, ns).headOption.flatMap(n => Matching.attrValue(n, q, ns))
      }

    /** Children elements at the element path.
      *
      * Examples (no namespaces):
      * {{{
      * val xml = <root><a><b/><b/></a></root>
      * xml.childrenAt(XMLPath.parse("root/a")) // List(<b/>, <b/>)
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <r xmlns:ns="urn:x"><ns:a><ns:b/><ns:b/></ns:a></r>
      * val ns  = Ns(Map("ns" -> "urn:x"))
      * xml.childrenAt(XMLPath.parse("r/ns:a"), ns) // List(<ns:b/>, <ns:b/>)
      * }}}
      */
    def childrenAt(path: XMLPath, ns: Ns = Ns.empty): List[Elem] =
      nodesAt(path, ns).collect { case el: Elem => el }.toList

    /** All child elements directly under any of the `under` paths.
      *
      * Examples (no namespaces):
      * {{{
      * val xml = <r><xs><x/><x/></xs><ys><y/></ys></r>
      * xml.selectUnder(List(XMLPath.parse("r/xs"), XMLPath.parse("r/ys")))
      * // List(<x/>, <x/>, <y/>)
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <r xmlns:ns="u"><ns:xs><ns:x/><ns:x/></ns:xs><ns:ys><ns:y/></ns:ys></r>
      * val ns  = Ns(Map("ns" -> "u"))
      * xml.selectUnder(List(XMLPath.parse("r/ns:xs"), XMLPath.parse("r/ns:ys")), ns)
      * // List(<ns:x/>, <ns:x/>, <ns:y/>)
      * }}}
      */
    def selectUnder(under: List[XMLPath], ns: Ns = Ns.empty): List[Elem] =
      under.flatMap(p => e.childrenAt(p, ns))

    /** First successful text among alternative paths.
      *
      * Examples (no namespaces):
      * {{{
      * val xml = <r><title2>T</title2></r>
      * val t1 = XMLPath.parse("r/title")
      * val t2 = XMLPath.parse("r/title2")
      * xml.firstText(List(t1, t2)) // Some("T")
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <r xmlns:cbc="u"><cbc:T2>T</cbc:T2></r>
      * val ns  = Ns(Map("cbc" -> "u"))
      * val p1  = XMLPath.parse("r/cbc:T1")
      * val p2  = XMLPath.parse("r/cbc:T2")
      * xml.firstText(List(p1, p2), ns) // Some("T")
      * }}}
      */
    def firstText(paths: List[XMLPath], ns: Ns = Ns.empty): Option[String] =
      paths.iterator.map(textAt(_, ns)).collectFirst { case Some(v) => v }

    /** First successful text among alternative paths or `ParserError`.
      *
      * Examples (no namespaces):
      * {{{
      * val xml = <r/>
      * xml.firstTextOrError(List(XMLPath.parse("r/title")), "title")
      * // Left(MissingField("title", Some("r/title")))
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <r xmlns:cbc="u"/>
      * val ns  = Ns(Map("cbc" -> "u"))
      * xml.firstTextOrError(List(XMLPath.parse("r/cbc:Title")), "Title", ns)
      * // Left(MissingField("Title", Some("r/cbc:Title")))
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
      * Examples (no namespaces):
      * {{{
      * val xml = <r><a id="1"/></r>
      * xml.firstAttr(List(XMLPath.parse("r/a/@id"))) // Some("1")
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <r xmlns:cbc="u"><a cbc:id="1"/></r>
      * val ns  = Ns(Map("cbc" -> "u"))
      * xml.firstAttr(List(XMLPath.parse("r/a/@cbc:id")), ns) // Some("1")
      * }}}
      */
    def firstAttr(paths: List[XMLPath], ns: Ns = Ns.empty): Option[String] =
      paths.iterator.map(attrAt(_, ns)).collectFirst { case Some(v) => v }

    /** First successful attribute among alternative `@attr` paths, else error.
      *
      * Examples (no namespaces):
      * {{{
      * val xml = <r/>
      * xml.firstAttrOrError(List(XMLPath.parse("r/a/@id")), "id")
      * // Left(MissingField("id", Some("r/a/@id")))
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <r xmlns:cbc="u"/>
      * val ns  = Ns(Map("cbc" -> "u"))
      * xml.firstAttrOrError(List(XMLPath.parse("r/a/@cbc:id")), "id", ns)
      * // Left(MissingField("id", Some("r/a/@cbc:id")))
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
      * Examples (no namespaces):
      * {{{
      * val xml = <r><xs><x/></xs><ys><y/></ys></r>
      * xml.childrenAtAll(List(XMLPath.parse("r/xs"), XMLPath.parse("r/ys")))
      * // List(<x/>, <y/>)
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <r xmlns:ns="u"><ns:xs><ns:x/></ns:xs><ns:ys><ns:y/></ns:ys></r>
      * val ns  = Ns(Map("ns" -> "u"))
      * xml.childrenAtAll(List(XMLPath.parse("r/ns:xs"), XMLPath.parse("r/ns:ys")), ns)
      * // List(<ns:x/>, <ns:y/>)
      * }}}
      */
    def childrenAtAll(paths: List[XMLPath], ns: Ns = Ns.empty): List[Elem] =
      paths.flatMap(e.childrenAt(_, ns))

    /** Try primary roots; if empty, try fallback roots (ONLY fallback).
      *
      * Examples (no namespaces):
      * {{{
      * val xml = <r><fallback><i/></fallback></r>
      * val prim = List(XMLPath.parse("r/primary"))
      * val fb   = List(XMLPath.parse("r/fallback"))
      * xml.childrenAtWithFallback(prim, fb) // List(<i/>)
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <r xmlns:ns="u"><ns:fb><i/></ns:fb></r>
      * val ns  = Ns(Map("ns" -> "u"))
      * val prim = List(XMLPath.parse("r/ns:prim"))
      * val fb   = List(XMLPath.parse("r/ns:fb"))
      * xml.childrenAtWithFallback(prim, fb, ns) // List(<i/>)
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
      * Examples (no namespaces):
      * {{{
      * val xml = <r><fb><t>a</t><t>b</t></fb></r>
      * val primRoots = List(XMLPath.parse("r/prim"))
      * val fbRoots   = List(XMLPath.parse("r/fb"))
      * val item      = XMLPath.parse("t")
      * xml.extractManyWithFallback(primRoots, fbRoots, item, item, "t")
      * // List(Right("a"), Right("b"))
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <r xmlns:ns="u"><ns:fb><ns:t>a</ns:t><ns:t>b</ns:t></ns:fb></r>
      * val ns  = Ns(Map("ns" -> "u"))
      * val primRoots = List(XMLPath.parse("r/ns:prim"))
      * val fbRoots   = List(XMLPath.parse("r/ns:fb"))
      * val primItem  = XMLPath.parse("ns:t")
      * val fbItem    = XMLPath.parse("ns:t")
      * xml.extractManyWithFallback(primRoots, fbRoots, primItem, fbItem, "t", ns)
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
      * Examples (no namespaces):
      * {{{
      * val xml = <r><fb><a id="1"/><a id="2"/></fb></r>
      * val primRoots = List(XMLPath.parse("r/prim"))
      * val fbRoots   = List(XMLPath.parse("r/fb"))
      * val item      = XMLPath.parse("a/@id")
      * xml.extractManyAttrWithFallback(primRoots, fbRoots, item, item, "id", "id")
      * // List(Right("1"), Right("2"))
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val xml = <r xmlns:cbc="u"><fb><a cbc:id="1"/><a cbc:id="2"/></fb></r>
      * val ns  = Ns(Map("cbc" -> "u"))
      * val primRoots = List(XMLPath.parse("r/prim"))
      * val fbRoots   = List(XMLPath.parse("r/fb"))
      * val item      = XMLPath.parse("a/@cbc:id")
      * xml.extractManyAttrWithFallback(primRoots, fbRoots, item, item, "id", "id", ns)
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
      * Examples (no namespaces):
      * {{{
      * val parents = List(<p><t>a</t></p>, <p/>)
      * val item    = XMLPath.parse("t")
      * parents.getTextsAt(List(XMLPath.parse("root")), item, "t")
      * // List(Right("a"), Left(MissingField("t", Some("root"))))
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val ns  = Ns(Map("ns" -> "u"))
      * val parents = List(<p xmlns:ns="u"><ns:t>a</ns:t></p>, <p xmlns:ns="u"/>)
      * val item    = XMLPath.parse("ns:t")
      * parents.getTextsAt(List(XMLPath.parse("root/ns:p")), item, "t", ns)
      * // List(Right("a"), Left(MissingField("t", Some("root/ns:p"))))
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
      * Examples (no namespaces):
      * {{{
      * val parents = List(<p id="1"/>, <p/>)
      * val item    = XMLPath.parse("@id")
      * parents.getAttrsAt(List(XMLPath.parse("root")), item, "id")
      * // List(Right("1"), Left(MissingField("id", Some("root"))))
      * }}}
      *
      * Examples (with namespaces):
      * {{{
      * val ns      = Ns(Map("cbc" -> "u"))
      * val parents = List(<p xmlns:cbc="u" cbc:id="1"/>, <p xmlns:cbc="u"/>)
      * val item    = XMLPath.parse("@cbc:id")
      * parents.getAttrsAt(List(XMLPath.parse("root/p")), item, "id", ns)
      * // List(Right("1"), Left(MissingField("id", Some("root/p"))))
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
