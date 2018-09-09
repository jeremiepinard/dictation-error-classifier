package com.dictation.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.language.postfixOps

class FrontendRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with FrontendRoutes {

  lazy val routes: Route = Route.seal(frontendRoutes)

  "Frontend routes (GET /)" should {
    "return the index.html file from the resources/dist directory" in {
      HttpRequest(uri = "/") ~> routes ~> check {
        status should be(StatusCodes.OK)
        contentType should be(ContentTypes.`text/html(UTF-8)`)
      }
    }
  }

  "Frontend routes (GET /{anything})" should {
    "return the {anything} file from the resources/dist directory if it exists" in {
      HttpRequest(uri = "/directory/test.js") ~> routes ~> check {
        status should be(StatusCodes.OK)
        contentType should be(MediaTypes.`application/javascript` withCharset HttpCharsets.`UTF-8`)
      }
    }

    "return the proper error if the {anything} file does not exist" in {
      HttpRequest(uri = "/directory/allo/non-existent.js") ~> routes ~> check {
        status should be(StatusCodes.NotFound)
        val body = entityAs[String]
        body.toString should be ("The requested resource could not be found.")
      }
    }
  }
}
