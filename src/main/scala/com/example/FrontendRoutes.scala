package com.example

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path

trait FrontendRoutes extends JsonSupport {
  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  private lazy val log = Logging(system, classOf[FrontendRoutes])

  lazy val frontendRoutes: Route =
    pathEndOrSingleSlash {
      redirect("/index.html", StatusCodes.PermanentRedirect)
    } ~
      path(Remaining) {
        asset =>
          getFromResource(s"dist/$asset")
      }
}
