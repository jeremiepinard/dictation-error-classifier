package com.example

//#quick-start-server
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scalaz.\/
import scalaz.std.either._

//#main-class
object QuickstartServer extends App with UserRoutes {

  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

  //#main-class
  // from the UserRoutes trait
  lazy val routes: Route = userRoutes
  //#main-class

  val port: Int = \/.fromTryCatchNonFatal(sys.env("PORT").toInt).getOrElse(8080)

  val port2: Int = \/.fromTryCatchNonFatal(system.settings.config.getInt("http.port")).getOrElse(8080)

  //#http-server
  Http().bindAndHandle(routes, "localhost", port)

  println(s"Server online at http://localhost:$port/$port2")

  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class
}
//#main-class
//#quick-start-server
