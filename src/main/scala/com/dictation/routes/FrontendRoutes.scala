package com.dictation.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path

trait FrontendRoutes {

  implicit def system: ActorSystem

  lazy val frontendRoutes: Route =
    pathEndOrSingleSlash {
      getFromResource(s"dist/index.html")
    } ~
      path(Remaining) {
        asset =>
          getFromResource(s"dist/$asset")
      }
}
