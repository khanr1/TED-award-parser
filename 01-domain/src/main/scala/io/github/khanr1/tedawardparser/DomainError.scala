package io.github.khanr1.tedawardparser

enum DomainError derives CanEqual:
  case Missing(field: String) // expected value absent
  case Invalid(field: String, detail: String) // value present but bad
  case Unexpected(detail: String) // anything else

  def message: String = this match
    case Missing(field)         => s"Missing: $field"
    case Invalid(field, detail) => s"$field invalid: $detail"
    case Unexpected(detail)     => detail
