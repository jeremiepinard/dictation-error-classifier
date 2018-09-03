package com.dictation

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.handleRejections
import akka.http.scaladsl.server.{Directive0, RejectionHandler}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

trait GenericRoutesDirectives {

  implicit def system: ActorSystem

  def corsWithRejections: Directive0 =
    handleRejections(corsRejectionHandler) & cors(CorsSettings(system)) & handleRejections(RejectionHandler.default)
}
