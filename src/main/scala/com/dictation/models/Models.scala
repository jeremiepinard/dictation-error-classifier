package com.dictation.models

import java.util.UUID

case object Models {

  final case class DictationInput(name: String, entries: Seq[String])

  final case class Dictation(id: UUID, name: String, entries: Seq[String])
  final case class Dictations(dictations: Seq[Dictation])
  final case class ErrorResponse(cause: String)
}
