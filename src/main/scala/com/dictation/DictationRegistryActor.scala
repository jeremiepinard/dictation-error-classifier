package com.dictation

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.dictation.models.Models._

import scalaz.syntax.std.boolean._

class DictationRegistryActor extends Actor with ActorLogging {
  import DictationRegistryActor._

  var dictations = Set(
    Dictation(UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1449"), "Les Animaux",Seq("aaa", "bbb")),
    Dictation(UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1450"), "Les Arbres", Seq("aaa", "bbb"))
  )

  def receive: Receive = {
    case GetDictations =>
      sender() ! Dictations(dictations.toSeq.sortBy(_.id))
    case UpdateDictation(dictationId, dictation) =>
      val origin = sender()
      def sameId: Dictation => Boolean = _.id.toString.equalsIgnoreCase(dictationId.toString)
      dictations
        .exists(sameId)
        .fold(
          {
            dictations = dictations.filterNot(sameId) + Dictationer(dictationId, dictation)
            origin ! Success
          },
          origin ! MissingDictation(dictationId.toString)
        )
  }
}

object DictationRegistryActor {
  trait CommandResult
  final case class MissingDictation(id: String) extends CommandResult
  final case object Success extends CommandResult

  final case object GetDictations
  final case class UpdateDictation(dictationId: UUID, dictation: DictationInput)

  def props: Props = Props[DictationRegistryActor]
}