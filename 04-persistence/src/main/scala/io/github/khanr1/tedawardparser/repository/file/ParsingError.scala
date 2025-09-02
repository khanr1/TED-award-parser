package io.github.khanr1.tedawardparser.repository.file

import scala.util.control.NoStackTrace

enum ParserError(val message: String, val path: Option[String])
    extends NoStackTrace:

  /** A required field is absent at the expected location. */
  case MissingField(field: String, at: Option[String] = None)
      extends ParserError(s"Missing required field: $field.", at)

  /** A field exists but cannot be converted to the expected type/format. */
  case InvalidFormat(
      field: String,
      expected: String,
      found: String,
      at: Option[String] = None
  ) extends ParserError(
        s"Invalid $field: expected $expected, found: $found",
        at
      )

  /** More than one value was found where one was expected. */
  case MultipleValues(field: String, at: Option[String] = None)
      extends ParserError(s"Multiple values for $field (expected one)", at)

  /** The structure contains a node we don’t support (version drift, wrong form,
    * etc.).
    */
  case UnexpectedNode(node: String, at: Option[String] = None)
      extends ParserError(s"Unexpected node: $node", at)

  /** Fallback for unforeseen issues that are still parse-related. */
  case Unknown(why: String, at: Option[String] = None)
      extends ParserError(why, at)
