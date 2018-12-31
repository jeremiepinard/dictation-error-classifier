package com.dictation.actors

import java.util.UUID

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}
import akka.pattern.ask
import akka.persistence.pg.journal.query.PostgresReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import com.dictation.models.Models._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import scala.util.control.NonFatal
import scalaz.\/
import scalaz.syntax.either._

class DictationRegistrer extends Actor with ActorLogging with Stash {
  import DictationRegistrer._

  implicit val timeout: Timeout = Timeout(5 seconds)

  implicit val ec: ExecutionContext = context.dispatcher

  private final val DictationActorNamePrefix: String = "dictation-actor-"

  private val readJournal = PersistenceQuery(context.system).readJournalFor[PostgresReadJournal](PostgresReadJournal.Identifier)

  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  def dictationActor(dictationId: UUID): ActorRef = {
    val name = s"$DictationActorNamePrefix${dictationId.toString}"
    context.child(name).getOrElse({
      log.info(s"creating new actor named [$name]")
      context.actorOf(DictationActor.props(dictationId), name)
    })
  }

  override def preStart() {
    // start all existing dictation actors
    val startActors: Future[Done] = readJournal
      .eventsByTags(Set("type" -> "dictation"), 0)
      .runWith(Sink.foreach(e => dictationActor(UUID.fromString(e.persistenceId))))
    // todo the stream never completes, so we cannot rely on the Future being completed here, find a way around this
    Thread.sleep(1000)
    self ! ActorsLoaded
  }

  override def receive: Receive = starting

  private def starting: Receive = {
    case ActorsLoaded =>
      unstashAll()
      context.become(started)

    case _ => stash()
  }

  private def started: Receive = {
    case GetDictations =>
      val origin = sender()
      val actors = context.children.filter(_.path.name.contains(DictationActorNamePrefix))
      Future.sequence(actors.map(
        actor => (actor ? DictationActor.GetDictation).mapTo[Option[Dictation]]
      )).map(
        dictations =>
          origin ! GetDictationsResult(Dictations(dictations.flatten.toSeq.sortBy(_.name)).right)
      ).recover {
        case NonFatal(ex) =>
          origin ! GetDictationsResult(ex.left)
      }

    case CreateDictation(dictation) =>
      val id = UUID.randomUUID()
      dictationActor(id) forward DictationActor.CreateDictation(dictation)

    case UpdateDictation(dictationId, dictation) =>
      dictationActor(dictationId) forward DictationActor.UpdateDictation(dictation)

    case DeleteDictation(dictationId) =>
      dictationActor(dictationId) forward DictationActor.DeleteDictation

    case other =>
      log.error(s"received unsupported message [$other]")
  }
}

object DictationRegistrer {
  private case object ActorsLoaded

  final case object GetDictations
  final case class GetDictationsResult(result: Throwable \/ Dictations)
  final case class CreateDictation(dictation: DictationInput)
  final case class UpdateDictation(dictationId: UUID, dictation: DictationInput)
  final case class DeleteDictation(dictationId: UUID)

  def props: Props = Props(new DictationRegistrer)
}