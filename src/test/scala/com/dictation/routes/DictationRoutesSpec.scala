package com.dictation.routes

import java.util.UUID

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestProbe}
import com.dictation.actors.DictationActor.{MissingDictation, Success}
import com.dictation.actors.DictationRegistrer.{CreateDictation, GetDictations, GetDictationsResult, UpdateDictation}
import com.dictation.models.Models.{Dictation, DictationInput, Dictations}
import com.dictation.{EntityMarshaller, JsonSupport}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import spray.json.JsArray
import scalaz.syntax.either._

import scala.language.postfixOps

class DictationRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with ApiRoutes with EntityMarshaller with JsonSupport with BeforeAndAfterAll {

  val storedDictations = Dictations(Seq(
    Dictation(UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1449"), "Les Animaux", Seq("aaa", "bbb")),
    Dictation(UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1450"), "Les Arbres", Seq("aaa", "bbb")))
  )

  private val newDictationId = UUID.randomUUID()

  private val probe = TestProbe()
  override val dictationRegistryActor: ActorRef = probe.ref

  override def beforeAll(): Unit = {
    probe.setAutoPilot((sender: ActorRef, msg: Any) => msg match {
      case GetDictations =>
        sender ! GetDictationsResult(storedDictations.right)
        TestActor.KeepRunning
      case UpdateDictation(id, _) =>
        id.toString match {
          case "aabc70f4-b432-11e8-96f8-529269fb1449" => sender ! MissingDictation(id)
          case "aaaaaaaa-b432-11e8-96f8-529269fb1449" => // when akka fails to write to the journal, the actor crashes, so it does not respond
          case uuid => sender ! Success(UUID.randomUUID())
        }
        TestActor.KeepRunning
      case CreateDictation(dictation) =>
        dictation.name match {
          case "persist-fails" =>
          case _ => sender ! Success(newDictationId)
        }
        TestActor.KeepRunning
    })
  }

  lazy val routes: Route = apiRoutes

  "Dictation routes (GET /dictations)" should {
    "return the existing dictations" in {
      val dictations = getDictations
      dictations should be(storedDictations)
    }
  }

  "Dictation routes (PUT /dictations/{uuid})" should {
    "properly update an existing dictation" in {
      val updatedDidaction = Dictation(
        id = UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1449"),
        name = "ouhhh la la!!",
        entries = Seq("word 1")
      )

      val updateRequest = HttpRequest(
        method = HttpMethods.PUT,
        uri = s"/api/v1/dictations/${updatedDidaction.id.toString}",
        entity = DictationInput(updatedDidaction.name, updatedDidaction.entries)
      )

      updateRequest ~> routes ~> check {
        status should be(StatusCodes.OK)
      }
    }


    "return a proper error when updating an non-existent dictation" in {
      val updateRequest = HttpRequest(
        method = HttpMethods.PUT,
        uri = "/api/v1/dictations/aabc70f4-b432-11e8-96f8-529269fb1449",
        entity = DictationInput("", Seq.empty)
      )

      updateRequest ~> routes ~> check {
        status should be(StatusCodes.NotFound)
        val body = entityAs[String]
        body.toString should be("The requested resource could not be found but may be available again in the future.")
      }
    }

    "return a proper error when updating with an invalid json body" in {
      val updateRequest = HttpRequest(
        method = HttpMethods.PUT,
        uri = "/api/v1/dictations/a8bc70f4-b432-11e8-96f8-529269fb1449",
        entity = JsArray.empty
      )

      updateRequest ~> routes ~> check {
        status should be(StatusCodes.BadRequest)
        val body = entityAs[String]
        body.toString should be("The request content was malformed:\nObject expected in field 'name'")
      }
    }

    "return a proper error when updating with a non-json body" in {
      val updateRequest = HttpRequest(
        method = HttpMethods.PUT,
        uri = "/api/v1/dictations/a8bc70f4-b432-11e8-96f8-529269fb1449",
        entity = "bleauh"
      )

      updateRequest ~> routes ~> check {
        status should be(StatusCodes.UnsupportedMediaType)
        val body = entityAs[String]
        body.toString should be("The request's Content-Type is not supported. Expected:\napplication/json")
      }
    }

    "return a proper error when a non-uuid is provided in the path" in {
      val updateRequest = HttpRequest(
        method = HttpMethods.PUT,
        uri = "/api/v1/dictations/non-uuid",
        entity = JsArray.empty
      )

      updateRequest ~> routes ~> check {
        status should be(StatusCodes.NotFound)
        val body = entityAs[String]
        body.toString should be("The requested resource could not be found.")
      }
    }

    "return a proper error when persisting to the akka-persistence journal fails(actor would not respond to message)" in {
      val updatedDidaction = Dictation(
        id = UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1449"),
        name = "ouhhh la la!!",
        entries = Seq("word 1")
      )

      val updateRequest = HttpRequest(
        method = HttpMethods.PUT,
        uri = "/api/v1/dictations/aaaaaaaa-b432-11e8-96f8-529269fb1449",
        entity = DictationInput(updatedDidaction.name, updatedDidaction.entries)
      )

      updateRequest ~> routes ~> check {
        status should be(StatusCodes.RequestTimeout)
        val body = entityAs[String]
        body.toString should be("The server timed out waiting for the request.")
      }
    }
  }

  "Dictation routes (POST /dictations/{uuid})" should {
    "properly create a dictation" in {
      val newDidaction = DictationInput(
        name = "ouhhh la la!!",
        entries = Seq("word 1")
      )

      val postRequest = HttpRequest(
        method = HttpMethods.POST,
        uri = s"/api/v1/dictations",
        entity = newDidaction
      )

      postRequest ~> routes ~> check {
        status should be(StatusCodes.Created)
        header("Location").get should be(Location(s"http://example.com/api/v1/dictations/$newDictationId"))
      }
    }

    "return a proper error when creating with an invalid json body" in {
      val postRequest = HttpRequest(
        method = HttpMethods.POST,
        uri = s"/api/v1/dictations",
        entity = JsArray.empty
      )

      postRequest ~> routes ~> check {
        status should be(StatusCodes.BadRequest)
        val body = entityAs[String]
        body.toString should be("The request content was malformed:\nObject expected in field 'name'")
      }
    }

    "return a proper error when creating with a non-json body" in {
      val postRequest = HttpRequest(
        method = HttpMethods.POST,
        uri = s"/api/v1/dictations",
        entity = "bleauh"
      )

      postRequest ~> routes ~> check {
        status should be(StatusCodes.UnsupportedMediaType)
        val body = entityAs[String]
        body.toString should be("The request's Content-Type is not supported. Expected:\napplication/json")
      }
    }

    "return a proper error when persisting to the akka-persistence journal fails(actor would not respond to message)" in {
      val newDidaction = DictationInput(
        name = "persist-fails",
        entries = Seq("word 1")
      )

      val postRequest = HttpRequest(
        method = HttpMethods.POST,
        uri = s"/api/v1/dictations",
        entity = newDidaction
      )

      postRequest ~> routes ~> check {
        status should be(StatusCodes.RequestTimeout)
        val body = entityAs[String]
        body.toString should be("The server timed out waiting for the request.")
      }
    }
  }

  private def getDictations: Dictations = {
    HttpRequest(uri = "/api/v1/dictations") ~> routes ~> check {
      status should be(StatusCodes.OK)
      contentType should be(ContentTypes.`application/json`)
      entityAs[Dictations]
    }
  }
}
