package com.dictation

//#quick-start-server
import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.stream.ActorMaterializer
import com.dictation.actors.{DictationActor, DictationRegistrer}
import com.dictation.routes.{ApiRoutes, FrontendRoutes}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scalaz.\/
import org.flywaydb.core.Flyway

object Server extends App with ApiRoutes with FrontendRoutes {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val dictationRegistryActor: ActorRef = system.actorOf(DictationRegistrer.props, "dictationRegistryActor")

  lazy val routes: Route =
    DebuggingDirectives.logRequestResult("", Logging.InfoLevel) {
      apiRoutes ~
        frontendRoutes
    }

  val port: Int = \/.fromTryCatchNonFatal(sys.env("PORT").toInt).getOrElse(8080)
println(sys.env)
  val dbUrl: String = \/.fromTryCatchNonFatal(sys.env("DB_URL")).getOrElse("jdbc:postgresql://127.0.0.1:5432/dictation_error_classifier")
  val dbUsername: String = \/.fromTryCatchNonFatal(sys.env("DB_USER")).getOrElse("dictation_error_classifier")
  val dbPassword: String = \/.fromTryCatchNonFatal(sys.env("DB_PASSWORD")).getOrElse("password")

  def startService(): Unit = {

    val flyway: Flyway = Flyway.configure().dataSource(dbUrl, dbUsername, dbPassword).load

    // Start the migration
    flyway.migrate()

    Http().bindAndHandle(routes, "0.0.0.0", port)

    println(s"Server online at http://0.0.0.0:$port")

    Await.result(system.whenTerminated, Duration.Inf)
  }

  startService()
}
