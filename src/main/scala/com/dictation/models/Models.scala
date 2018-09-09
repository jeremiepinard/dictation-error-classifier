package com.dictation.models

import java.util.UUID

case object Models {

  final case class DictationInput(name: String, entries: Seq[String])

  final case class Dictation(id: UUID, name: String, entries: Seq[String])

  object Dictationer {
    def apply(id: UUID, input: DictationInput): Dictation = {
      Dictation(id, input.name, input.entries)
    }
  }

  final case class Dictations(dictations: Seq[Dictation])

  final case class ErrorResponse(cause: String)
}
