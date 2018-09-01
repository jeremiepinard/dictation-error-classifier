package com.dictation

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path

trait FrontendRoutes {
  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  private lazy val log = Logging(system, classOf[FrontendRoutes])

  lazy val frontendRoutes: Route =
    pathEndOrSingleSlash {
      getFromResource(s"dist/index.html")
    } ~
      path(Remaining) {
        asset =>
          getFromResource(s"dist/$asset")
      }
}
