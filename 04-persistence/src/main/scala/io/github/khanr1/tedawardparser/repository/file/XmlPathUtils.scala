package io.github.khanr1.tedawardparser
package repository
package file

import scala.xml.{Elem, Node, NodeSeq}

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
    def nodesAt(path: XMLPath, ns: Ns = Ns.empty): NodeSeq =
      path.elementPrefix.foldLeft(e: NodeSeq) {
        case (curr, Segment.Elem(q)) =>
          curr.flatMap(_.child.collect {
            case el: Elem if Matching.elemMatches(el, q, ns) => el
          })
        case (curr, _) => curr
      }

    /** First node’s normalized text at element path. */
    def textAt(path: XMLPath, ns: Ns = Ns.empty): Option[String] =
      nodesAt(path, ns).headOption
        .map(_.text.replaceAll("\\s+", " ").trim)
        .filter(_.nonEmpty)

    /** Attribute value when path ends with @attr. */
    def attrAt(path: XMLPath, ns: Ns = Ns.empty): Option[String] =
      path.attrQName.flatMap { q =>
        nodesAt(path, ns).headOption.flatMap(n => Matching.attrValue(n, q, ns))
      }

    /** Children elements at the element path. */
    def childrenAt(path: XMLPath, ns: Ns = Ns.empty): List[Elem] =
      nodesAt(path, ns).collect { case el: Elem => el }.toList

    /** First successful text among alternative paths. */
    def firstText(paths: List[XMLPath], ns: Ns = Ns.empty): Option[String] =
      paths.iterator.map(textAt(_, ns)).collectFirst { case Some(v) => v }

    /** First successful attribute among alternative @attr paths. */
    def firstAttr(paths: List[XMLPath], ns: Ns = Ns.empty): Option[String] =
      paths.iterator.map(attrAt(_, ns)).collectFirst { case Some(v) => v }
