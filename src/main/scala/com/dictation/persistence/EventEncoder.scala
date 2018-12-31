package com.dictation.persistence

import akka.persistence.pg.JsonString
import akka.persistence.pg.event.JsonEncoder
import com.dictation.JsonSupport
import com.dictation.actors.DictationActor.{DictationCreatedEvent, DictationDeletedEvent, DictationUpdatedEvent}
import spray.json.DefaultJsonProtocol.jsonFormat1
import spray.json.RootJsonFormat
import spray.json._

class EventEncoder extends JsonEncoder with JsonSupport {

  implicit def dictationCreatedEventJsonFormat: RootJsonFormat[DictationCreatedEvent] = jsonFormat1(DictationCreatedEvent)
  implicit def dictationUpdatedEventJsonFormat: RootJsonFormat[DictationUpdatedEvent] = jsonFormat1(DictationUpdatedEvent)

  override def toJson: PartialFunction[Any, JsonString] = {
    case e: DictationCreatedEvent =>
      val allo = dictationCreatedEventJsonFormat.write(e)
      JsonString(allo.toString())
    case e: DictationUpdatedEvent => JsonString(dictationUpdatedEventJsonFormat.write(e).toString())
    case e: DictationDeletedEvent => JsonString("")
  }

  override def fromJson: PartialFunction[(JsonString, Class[_]), AnyRef] = {
    case (json, c) if c == classOf[DictationCreatedEvent] => dictationCreatedEventJsonFormat.read(json.value.parseJson)
    case (json, c) if c == classOf[DictationUpdatedEvent] => dictationUpdatedEventJsonFormat.read(json.value.parseJson)
    case (_, c) if c == classOf[DictationDeletedEvent] => DictationDeletedEvent()
  }
}
