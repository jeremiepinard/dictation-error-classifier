package com.dictation

import java.util.UUID
import akka.actor.{Actor, ActorLogging, Props}
import com.dictation.models.Models._
import scalaz.syntax.std.boolean._

class DictationRegistryActor extends Actor with ActorLogging {
  import DictationRegistryActor._

  var dictations = Set(
    Dictation(UUID.randomUUID().toString, "Les Animaux",Seq("aaa", "bbb")),
    Dictation(UUID.randomUUID().toString, "Les Arbres", Seq("aaa", "bbb"))
  )

  def receive: Receive = {
    case GetDictations =>
      sender() ! Dictations(dictations.toSeq)
    case UpdateDictation(dictationId, dictation) =>
      val origin = sender()
      dictations
        .exists(_.id.equals(dictationId))
        .fold(
          {
            // todo modify set here ;)
          dictations
          },
          origin ! MissingDictation
        )
  }
}

object DictationRegistryActor {
  trait CommandResult
  final case class MissingDictation(id: String) extends CommandResult
  final case object Success extends CommandResult

  final case object GetDictations
  final case class UpdateDictation(dictationId: UUID, dictation: Dictation)

  def props: Props = Props[DictationRegistryActor]
}