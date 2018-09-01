package com.dictation

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class DictationRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with DictationRoutes {

  override val dictationRegistryActor: ActorRef =
    system.actorOf(DictationRegistryActor.props, "dictationRegistry")

  lazy val routes = dictationsRoutes

  "FrontendRoutes" should {
    "return no users if no present (GET /users)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/dictations")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"users":[]}""")
      }
    }
  }
}
