package com.dictation.actors

import java.util.UUID

import akka.actor.{ ActorLogging, Props }
import akka.persistence.PersistentActor
import akka.persistence.pg.event.{ JsonEncoder, Tagged }
import com.dictation.models.Models._

class DictationActor(dictationId: UUID) extends PersistentActor with ActorLogging {
  import DictationActor._

  override def persistenceId: String = dictationId.toString

  private var dictationState: Option[DictationInput] = None

  private def updateDictation(dictation: DictationInput): Unit = {
    dictationState = Some(dictation)
  }

  private def deleteDictation: Unit = {
    dictationState = None
  }

  val receiveRecover: Receive = {
    case evt: DictationCreatedEvent => updateDictation(evt.dictation)
    case evt: DictationUpdatedEvent => updateDictation(evt.dictation)
    case evt: DictationDeletedEvent => deleteDictation
  }

  def receiveCommand: Receive = {
    case GetDictation =>
      sender() ! dictationState.map(d => Dictation(dictationId, d.name, d.entries))

    case CreateDictation(dictation) =>
      val origin = sender()
      persist(DictationCreatedEvent(dictation)) { event =>
        updateDictation(event.dictation)
        origin ! Success(dictationId)
      }

    case UpdateDictation(dictation) =>
      val origin = sender()
      dictationState.fold(
        origin ! MissingDictation(dictationId)) {
          _ =>
            persist(DictationUpdatedEvent(dictation)) { event =>
              updateDictation(event.dictation)
              origin ! Success(dictationId)
            }
        }

    case DeleteDictation =>
      val origin = sender()
      dictationState.fold(
        origin ! MissingDictation(dictationId)) {
          _ =>
            persist(DictationDeletedEvent) { event =>
              deleteDictation
              origin ! Success(dictationId)
            }
        }

    case other =>
      log.error(s"received unsupported message [$other]")
  }

}

object DictationActor {
  trait CommandResult
  final case class MissingDictation(id: UUID) extends CommandResult
  final case class Success(id: UUID) extends CommandResult

  final case object GetDictation
  final case class CreateDictation(dictation: DictationInput)
  final case class UpdateDictation(dictation: DictationInput)
  final case object DeleteDictation

  trait DictationEvent extends Tagged {
    override def tags: Map[String, String] = Map("type" -> "dictation")
  }

  case class DictationCreatedEvent(dictation: DictationInput) extends DictationEvent
  case class DictationUpdatedEvent(dictation: DictationInput) extends DictationEvent
  case class DictationDeletedEvent() extends DictationEvent

  def props(id: UUID): Props = Props(new DictationActor(id))
}