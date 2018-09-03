package com.dictation

//#quick-start-server
import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scalaz.\/

object QuickstartServer extends App with ApiRoutes with FrontendRoutes {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val dictationRegistryActor: ActorRef = system.actorOf(DictationRegistryActor.props, "dictationRegistryActor")

  lazy val routes: Route =
    DebuggingDirectives.logRequestResult("", Logging.InfoLevel) {
      apiRoutes ~
        frontendRoutes
    }

  val port: Int = \/.fromTryCatchNonFatal(sys.env("PORT").toInt).getOrElse(8080)

  Http().bindAndHandle(routes, "0.0.0.0", port)

  println(s"Server online at http://0.0.0.0:$port")

  Await.result(system.whenTerminated, Duration.Inf)
}
