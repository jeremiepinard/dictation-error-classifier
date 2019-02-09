package com.dictation

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.dictation.models.Models.{ Dictation, DictationInput, Dictations, ErrorResponse }
import spray.json.{ DefaultJsonProtocol, JsString, JsValue, JsonFormat, RootJsonFormat, deserializationError }

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit object UUIDJsonFormat extends JsonFormat[UUID] {
    def write(x: UUID) = JsString(x.toString)
    def read(value: JsValue) = value match {
      case JsString(x) => UUID.fromString(x)
      case x => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }

  implicit def dictationInputJsonFormat = jsonFormat2(DictationInput)

  implicit val dictationJsonFormat: RootJsonFormat[Dictation] = jsonFormat3(Dictation)
  implicit val dictationsJsonFormat: RootJsonFormat[Dictations] = jsonFormat1(Dictations)

  implicit val errorResponseJsonFormat: RootJsonFormat[ErrorResponse] = jsonFormat1(ErrorResponse)
}
