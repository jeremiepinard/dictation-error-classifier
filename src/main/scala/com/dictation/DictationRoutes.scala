package com.dictation

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.dictation.DictationRegistryActor._
import com.dictation.models.Models._

import scala.concurrent.Future
import scala.concurrent.duration._

//#json-support

trait DictationRoutes extends JsonSupport {
  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  private lazy val log = Logging(system, classOf[DictationRoutes])

  def dictationRegistryActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val dictationsRoutes: Route =
    cors() {
      pathPrefix("dictations") {
        pathEnd {
          get {
            val dictations: Future[Dictations] = (dictationRegistryActor ? GetDictations).mapTo[Dictations]
            complete(dictations)
          }
        } ~
          path(JavaUUID) {
            id =>
              pathEnd {
                put {
                  extractExecutionContext {
                    implicit ec =>
                      entity(as[Dictation]) {
                        dictation =>
                          val result: Future[StatusCode] = (dictationRegistryActor ? UpdateDictation(id, dictation))
                            .mapTo[CommandResult]
                            .map {
                              case Success => StatusCodes.OK
                              case MissingDictation(missingId) =>
                                // todo return error payload as well
                                StatusCodes.NotFound
                            }
                          complete(result)
                      }
                  }
                }
              }
          }
      }
    }
}
