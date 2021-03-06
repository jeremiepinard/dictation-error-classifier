package com.dictation.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model.{HttpResponse, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.dictation.JsonSupport
import com.dictation.actors.DictationActor.{CommandResult, MissingDictation, Success}
import com.dictation.actors.DictationRegistrer._
import com.dictation.models.Models._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

//#json-support

trait DictationRoutes extends JsonSupport {
  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  private lazy val log = Logging(system, classOf[DictationRoutes])

  def dictationRegistryActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout: Timeout = Timeout(500 millis) // usually we'd obtain the timeout from the system's configuration

  lazy val dictationsRoutes: Route =
    pathPrefix("dictations") {
      (pathEnd & get) {
        val dictations: Future[GetDictationsResult] = (dictationRegistryActor ? GetDictations).mapTo[GetDictationsResult]

        onSuccess(dictations) {
          _.result.fold(
            error => failWith(error),
            d => complete(d),
          )
        }
      } ~
        pathEnd {
          extractExecutionContext {
            implicit ec => {
              extractRequestContext {
                requestContext => {
                  (post & entity(as[DictationInput])) {
                    dictation =>
                      val result: Future[HttpResponse] = (dictationRegistryActor ? CreateDictation(dictation))
                        .mapTo[CommandResult]
                        .map {
                          case Success(id) =>
                            val location = requestContext.request.uri.copy(path = requestContext.request.uri.path / id.toString)
                            HttpResponse(StatusCodes.Created, scala.collection.immutable.Seq(Location(location)))
                          case _ => HttpResponse(StatusCodes.InternalServerError)
                        }
                      complete(result)
                  }
                }
              }
            }
          }
        } ~
        (path(JavaUUID) & pathEnd) {
          id =>
            extractExecutionContext {
              implicit ec => {
                (put & entity(as[DictationInput])) {
                  dictation =>
                    val result: Future[StatusCode] = (dictationRegistryActor ? UpdateDictation(id, dictation))
                      .mapTo[CommandResult]
                      .map {
                        case Success(_) => StatusCodes.OK
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