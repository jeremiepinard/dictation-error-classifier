package com.dictation.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.dictation.models.Models._


import scalaz.syntax.std.boolean._

class DictationRegistrer extends Actor with ActorLogging {
  import DictationRegistrer._

  private var dictations: Set[Dictation] = Set.empty

  def receive: Receive = {
    case GetDictations =>
      sender() ! Dictations(dictations.toSeq.sortBy(_.id))

    case CreateDictation(dictation) =>
      val origin = sender()
      val id = UUID.randomUUID()
      dictations = dictations + Dictationer(id, dictation)
      origin ! Success(id)

    case UpdateDictation(dictationId, dictation) =>
      val origin = sender()
      def sameId: Dictation => Boolean = _.id.toString.equalsIgnoreCase(dictationId.toString)
      dictations
        .exists(sameId)
        .fold(
          {
            dictations = dictations.filterNot(sameId) + Dictationer(dictationId, dictation)
            origin ! Success(dictationId)
          },
          origin ! MissingDictation(dictationId)
        )

    case DeleteDictation(dictationId) =>
      val origin = sender()
      def sameId: Dictation => Boolean = _.id.toString.equalsIgnoreCase(dictationId.toString)
      dictations
        .exists(sameId)
        .fold(
          {
            dictations = dictations.filterNot(sameId)
            origin ! Success(dictationId)
          },
          origin ! MissingDictation(dictationId)
        )
  }
}

object DictationRegistrer {
  trait CommandResult
  final case class MissingDictation(id: UUID) extends CommandResult
  final case class AlreadyExists(id: UUID) extends CommandResult
  final case class Success(id: UUID) extends CommandResult

  final case object GetDictations
  final case class CreateDictation(dictation: DictationInput)
  final case class UpdateDictation(dictationId: UUID, dictation: DictationInput)
  final case class DeleteDictation(dictationId: UUID)

  def props: Props = Props[DictationRegistrer]
}