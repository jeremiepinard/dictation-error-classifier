package com.dictation

//#quick-start-server
import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.stream.ActorMaterializer
import com.dictation.actors.{ DictationActor, DictationRegistrer }
import com.dictation.routes.{ ApiRoutes, FrontendRoutes }

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

  private def envVar(name: String, default: String): String = {
    \/.fromTryCatchNonFatal(sys.env(name))
      .fold(
        err => {
          system.log.warning(s"could not find variable named [$name], using default")
          default
        },
        success => {
          system.log.info(s"properly found variable named [$name]")
          success
        })
  }

  val port: Int = envVar("PORT", "8080").toInt

  val dbUrl: String = envVar("DB_URL", "jdbc:postgresql://127.0.0.1:5432/dictation_error_classifier")
  val dbUsername: String = envVar("DB_USER", "dictation_error_classifier")
  val dbPassword: String = envVar("DB_PASSWORD", "password")

  def startService(): Unit = {

    val flyway: Flyway = Flyway.configure().dataSource(dbUrl, dbUsername, dbPassword).load

    // Start the migration
    flyway.migrate()

    Http().bindAndHandle(routes, "0.0.0.0", port)

    system.log.info(s"Server online at http://0.0.0.0:$port")

    Await.result(system.whenTerminated, Duration.Inf)
  }

  startService()
}
