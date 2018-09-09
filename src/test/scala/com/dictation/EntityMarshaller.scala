package com.dictation

import akka.http.scaladsl.marshalling.{Marshal, Marshaller}
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.unmarshalling.Unmarshaller
import org.scalatest.concurrent.ScalaFutures
import spray.json._

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

trait EntityMarshaller {

  this: ScalaFutures =>

  implicit def marshallEntity[T](e: T)(implicit m: Marshaller[T, RequestEntity], ec: ExecutionContext): RequestEntity = Marshal(e).to[RequestEntity].futureValue

  implicit val boxListUnmarshaller: Unmarshaller[String, JsValue] = Unmarshaller.strict(_.parseJson)
}
