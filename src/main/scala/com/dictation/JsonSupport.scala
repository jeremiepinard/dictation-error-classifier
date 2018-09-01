package com.dictation

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.dictation.models.Models.{Dictation, Dictations, ErrorResponse}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val dictationJsonFormat: RootJsonFormat[Dictation] = jsonFormat3(Dictation)
  implicit val dictationsJsonFormat: RootJsonFormat[Dictations] = jsonFormat1(Dictations)

  implicit val errorResponseJsonFormat: RootJsonFormat[ErrorResponse] = jsonFormat1(ErrorResponse)
}
