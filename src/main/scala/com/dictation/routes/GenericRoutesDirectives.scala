package com.dictation.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Directive0, Directives, RejectionHandler}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

trait GenericRoutesDirectives extends Directives {

  implicit def system: ActorSystem

  def corsWithRejections: Directive0 =
    handleRejections(corsRejectionHandler) & cors(CorsSettings(system)) & handleRejections(RejectionHandler.default)
}
