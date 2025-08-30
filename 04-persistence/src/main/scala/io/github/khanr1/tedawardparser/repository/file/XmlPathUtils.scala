package io.github.khanr1.tedawardparser
package repository
package file

import scala.xml.{Elem, Node, NodeSeq}
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.repository.file.Matching.elemMatches
import scala.util.CommandLineParser.ParseError

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

object XMLPathUtils:

  extension (p: XMLPath)
    def isAttrPath: Boolean =
      p.segments.lastOption.exists(_.isInstanceOf[Segment.Attr])

    /** The element-only prefix of the path (i.e., strip trailing @attr if
      * present).
      */
    def elementPrefix: Vector[Segment] =
      if isAttrPath then p.segments.dropRight(1) else p.segments

    def attrQName: Option[QName] =
      p.segments.lastOption.collect { case Segment.Attr(q) => q }

  extension (e: Elem)
    /** Resolve element steps of a path to nodes (attributes ignored here). */
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

    /** First node’s normalized text at element path. */
    def textAt(path: XMLPath, ns: Ns = Ns.empty): Option[String] =
      nodesAt(path, ns).headOption
        .map(_.text.replaceAll("\\s+", " ").trim)
        .filter(_.nonEmpty)

    /** First node`s normalized text at elemenet path or Error */
    def textAtOrError(
        path: XMLPath,
        field: String,
        ns: Ns = Ns.empty
    ): Either[ParserError, String] =
      textAt(path, ns).toRight(ParserError.MissingField(field, Some(path.show)))

    /** All text at element path. */
    def allTextAt(
        validPath: List[XMLPath],
        itemTag: XMLPath
    ): List[Option[String]] =
      val children = validPath.flatMap(p => e.childrenAt(p))
      children.map(elem => elem.textAt(itemTag))

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

    /** Attribute value when path ends with @attr. */
    def attrAt(path: XMLPath, ns: Ns = Ns.empty): Option[String] =
      path.attrQName.flatMap { q =>
        nodesAt(path, ns).headOption.flatMap(n => Matching.attrValue(n, q, ns))
      }

    /** Children elements at the element path. */
    def childrenAt(path: XMLPath, ns: Ns = Ns.empty): List[Elem] =
      nodesAt(path, ns).collect { case el: Elem => el }.toList

    /** All child elems directly under any of the 'under' paths. */
    def selectUnder(under: List[XMLPath], ns: Ns = Ns.empty): List[Elem] =
      under.flatMap(p => e.childrenAt(p, ns))

    /** First successful text among alternative paths. */
    def firstText(paths: List[XMLPath], ns: Ns = Ns.empty): Option[String] =
      paths.iterator.map(textAt(_, ns)).collectFirst { case Some(v) => v }

    /** First successful text among alternative paths or error */
    def firstTextOrError(
        paths: List[XMLPath],
        field: String,
        ns: Ns = Ns.empty
    ): Either[ParserError, String] =
      firstText(paths, ns).toRight(
        ParserError.MissingField(field, Some(paths.showAltPath()))
      )

    /** First successful attribute among alternative @attr paths. */
    def firstAttr(paths: List[XMLPath], ns: Ns = Ns.empty): Option[String] =
      paths.iterator.map(attrAt(_, ns)).collectFirst { case Some(v) => v }

    /** First successful attribute among alternative @attr paths. or Error */
    def firstAttrOrError(
        paths: List[XMLPath],
        field: String,
        ns: Ns = Ns.empty
    ): Either[ParserError, String] =
      firstAttr(paths, ns).toRight(
        ParserError.MissingField(field, Some(paths.showAltPath()))
      )

    /** Collect child elements for each path, concatenated. */
    def childrenAtAll(paths: List[XMLPath], ns: Ns = Ns.empty): List[Elem] =
      paths.flatMap(e.childrenAt(_, ns))

    /** Try primary roots; if empty, try fallback roots (ONLY fallback). */
    def childrenAtWithFallback(
        primary: List[XMLPath],
        fallback: List[XMLPath],
        ns: Ns = Ns.empty
    ): List[Elem] =
      val prim = e.childrenAtAll(primary, ns)
      if prim.nonEmpty then prim else e.childrenAtAll(fallback, ns)

  extension (l: List[Elem])
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
    def showAltPath(sep: String = "\n") = paths.map(p => p.show).mkString(sep)
