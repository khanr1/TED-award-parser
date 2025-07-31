package io.github.khanr1.tedawardparser
package repository
package file

import scala.xml.{Elem, NodeSeq}

extension (elem: Elem)
  def resolvePath(path: String*) = path
    .foldLeft(elem: NodeSeq)(_ \ _)
    .headOption
  def getText(paths: String*): Option[String] = resolvePath(paths: _*)
    .map(_.text.replaceAll("\\s+", " ").trim())
  def getAttributes(attribute: String, paths: String*): Option[List[String]] =
    resolvePath(paths: _*) match
      case None => None
      case Some(nodes) =>
        Some(
          nodes
            .flatMap { x => x.attribute(attribute) }
            .map(x => x.text.trim())
            .toList
        )
  def getAttr(attribute: String, paths: String*): Option[String] =
    getAttributes(attribute, paths: _*).flatMap(x => x.headOption)
