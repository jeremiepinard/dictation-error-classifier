package com.dictation

import java.util.UUID

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.dictation.models.Models.{Dictation, DictationInput, Dictations}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import spray.json.JsArray

import scala.language.postfixOps

class DictationRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with DictationRoutes with EntityMarshaller with JsonSupport {

  override val dictationRegistryActor: ActorRef =
    system.actorOf(DictationRegistryActor.props, "dictationRegistry")

  lazy val routes: Route = Route.seal(dictationsRoutes)

  "Dictation routes (GET /dictations)" should {
    "return the existing dictations" in {
      val dictations = getDictations

      dictations should be(Dictations(Seq(
        Dictation(UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1449"), "Les Animaux", Seq("aaa", "bbb")),
        Dictation(UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1450"), "Les Arbres", Seq("aaa", "bbb")))
      ))
    }

    "return the existing dictations ordered by didaction id (GET /dictations)" in {
      // todo create stuff and make sure they are return ordered
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
        uri = s"/dictations/${updatedDidaction.id.toString}",
        entity = DictationInput(updatedDidaction.name, updatedDidaction.entries)
      )

      updateRequest ~> routes ~> check {
        status should be(StatusCodes.OK)
      }

      getDictations should be(Dictations(Seq(
        updatedDidaction,
        Dictation(UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1450"), "Les Arbres", Seq("aaa", "bbb")))
      ))
    }


    "return a proper error when updating an non-existent dictation" in {
      val updateRequest = HttpRequest(
        method = HttpMethods.PUT,
        uri = "/dictations/aabc70f4-b432-11e8-96f8-529269fb1449",
        entity = DictationInput("", Seq.empty)
      )

      updateRequest ~> routes ~> check {
        status should be(StatusCodes.NotFound)
        val body = entityAs[String]
        body.toString should be ("The requested resource could not be found but may be available again in the future.")
      }
    }

    "return a proper error when updating with an invalid json body" in {
      val updateRequest = HttpRequest(
        method = HttpMethods.PUT,
        uri = "/dictations/a8bc70f4-b432-11e8-96f8-529269fb1449",
        entity = JsArray.empty
      )

      updateRequest ~> routes ~> check {
        status should be(StatusCodes.BadRequest)
        val body = entityAs[String]
        body.toString should be ("The request content was malformed:\nObject expected in field 'name'")
      }
    }

    "return a proper error when updating with a non-json body" in {
      val updateRequest = HttpRequest(
        method = HttpMethods.PUT,
        uri = "/dictations/a8bc70f4-b432-11e8-96f8-529269fb1449",
        entity = "bleauh"
      )

      updateRequest ~> routes ~> check {
        status should be(StatusCodes.UnsupportedMediaType)
        val body = entityAs[String]
        body.toString should be ("The request's Content-Type is not supported. Expected:\napplication/json")
      }
    }

    "return a proper error when a non-uuid is provided in the path" in {
      val updateRequest = HttpRequest(
        method = HttpMethods.PUT,
        uri = "/dictations/non-uuid",
        entity = JsArray.empty
      )

      updateRequest ~> routes ~> check {
        status should be(StatusCodes.NotFound)
        val body = entityAs[String]
        body.toString should be ("The requested resource could not be found.")
      }
    }
  }

  private def getDictations: Dictations =
    HttpRequest(uri = "/dictations") ~> routes ~> check {
      status should be(StatusCodes.OK)
      contentType should be(ContentTypes.`application/json`)
      entityAs[Dictations]
    }
}
