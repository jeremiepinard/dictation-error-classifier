package com.dictation.models

import spray.json.DefaultJsonProtocol

case object Models {

  final case class Dictation(id: String, name: String, entries: Seq[String])
  final case class Dictations(dictations: Seq[Dictation])
  final case class ErrorResponse(cause: String)

  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit def dictationJsonFormat = jsonFormat3(Dictation)
  implicit def dictationsJsonFormat = jsonFormat1(Dictations)
  implicit def errorResponseJsonFormat = jsonFormat1(ErrorResponse)
}
