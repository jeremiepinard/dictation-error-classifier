package com.dictation.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.pattern.AskTimeoutException

trait ApiRoutes extends DictationRoutes with GenericRoutesDirectives {

  implicit def system: ActorSystem

  val exceptionHandler = ExceptionHandler {
    case _: AskTimeoutException =>
      extractUri { uri =>
        complete(StatusCodes.RequestTimeout)
      }
  }

  lazy val apiRoutes: Route =
    pathPrefix("api") {
      Route.seal (
        corsWithRejections {
          handleExceptions(exceptionHandler) {
            pathPrefix("v1") {
              dictationsRoutes
            }
          }
        }
      )
    }
}
