package com.example

//#quick-start-server
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scalaz.\/
import akka.http.scaladsl.server.Directives._

//#main-class
object QuickstartServer extends App with UserRoutes with FrontendRoutes {

  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

  //#main-class
  // from the FrontendRoutes trait
  lazy val routes: Route = userRoutes ~ frontendRoutes
  //#main-class

  val port: Int = \/.fromTryCatchNonFatal(sys.env("PORT").toInt).getOrElse(8080)

  //#http-server
  Http().bindAndHandle(routes, "0.0.0.0", port)

  println(s"Server online at http://0.0.0.0:$port")

  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class
}
//#main-class
//#quick-start-server
