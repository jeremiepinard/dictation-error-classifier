package com.dictation

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

trait ApiRoutes extends DictationRoutes with GenericRoutesDirectives {

  implicit def system: ActorSystem

  lazy val apiRoutes: Route =
    pathPrefix("api") {
      Route.seal (
        corsWithRejections {
          pathPrefix("v1") {
            dictationsRoutes
          }
        }
      )
    }
}
