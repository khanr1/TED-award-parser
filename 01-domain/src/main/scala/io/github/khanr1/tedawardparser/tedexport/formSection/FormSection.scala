package io.github.khanr1.tedawardparser.tedExport.formSection

sealed trait FormSection
object FormSection:
  final case class ContractSection(value: ContractAward) extends FormSection
