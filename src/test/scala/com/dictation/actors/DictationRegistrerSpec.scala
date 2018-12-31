package com.dictation.actors

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.dictation.actors.DictationActor.{CommandResult, MissingDictation, Success}
import com.dictation.actors.DictationRegistrer._
import com.dictation.models.Models.{Dictation, DictationInput, Dictations}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz.syntax.either._

class DictationRegistrerSpec extends TestKit(ActorSystem()) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterEach {

  implicit val timeout: Timeout = Timeout(2 seconds)

  implicit val ec: ExecutionContext = system.dispatcher

  val dictationRegistrer: ActorRef = system.actorOf(DictationRegistrer.props, "dictationRegistry")

  override def beforeEach(): Unit = {
    super.beforeEach()
    dictationRegistrer ! GetDictations
    val dictations = expectMsgType[GetDictationsResult]

    val results = dictations.result
      .getOrElse(Dictations(Seq.empty))
      .dictations
      .map {
        dictation =>
          (dictationRegistrer ? DeleteDictation(dictation.id))
            .mapTo[CommandResult]
      }
    Await.ready(Future.sequence(results), 3 seconds)
  }

  "Fetching dictations" should {
    "return the existing dictations" in {
      val dictation1 = createRandomDictation()
      val dictation2 = createRandomDictation()

      val dictations = Dictations(Seq(dictation1, dictation2).sortBy(_.name))

      dictationRegistrer ! GetDictations
      expectMsg[GetDictationsResult](GetDictationsResult(dictations.right))
    }

    "return the existing dictations ordered by name (GET /dictations)" in {
      val dictation1 = createRandomDictation(name = "AAAA")
      val dictation2 = createRandomDictation(name = "CCCC")
      val dictation3 = createRandomDictation(name = "BBB")

      val dictations = Dictations(Seq(dictation1, dictation2, dictation3).sortBy(_.name))

      dictationRegistrer ! GetDictations
      expectMsg[GetDictationsResult](GetDictationsResult(dictations.right))
    }
  }

  "Updating didactions" should {
    "properly update an existing dictation" in {
      val dictation = createRandomDictation()
      val updatedDidaction = Dictation(
        id = dictation.id,
        name = "aie aie aie",
        entries = Seq("word xxxxx")
      )

      val dictations = Dictations(Seq(updatedDidaction).sortBy(_.name))

      dictationRegistrer ! UpdateDictation(updatedDidaction.id, DictationInput(updatedDidaction.name, updatedDidaction.entries))
      expectMsgType[Success]

      dictationRegistrer ! GetDictations
      val didactions = expectMsg[GetDictationsResult](GetDictationsResult(dictations.right))
    }

    "return a proper error when updating an non-existent dictation" in {
      val updatedDidaction = Dictation(
        id = UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1400"),
        name = "ouhhh la la!!",
        entries = Seq.empty
      )

      dictationRegistrer ! UpdateDictation(updatedDidaction.id, DictationInput(updatedDidaction.name, updatedDidaction.entries))
      expectMsgType[MissingDictation]
    }
  }

  "Deleting didactions" should {
    "properly delete an existing dictation" in {
      val dictation = createRandomDictation()

      dictationRegistrer ! GetDictations
      expectMsg[GetDictationsResult](GetDictationsResult(Dictations(Seq(dictation)).right))

      dictationRegistrer ! DeleteDictation(dictation.id)
      expectMsgType[Success]

      dictationRegistrer ! GetDictations
      expectMsg[GetDictationsResult](GetDictationsResult(Dictations(Seq.empty).right))
    }

    "return a proper error when deleting an non-existent dictation" in {
      val dictation = createRandomDictation()

      dictationRegistrer ! GetDictations
      expectMsg[GetDictationsResult](GetDictationsResult(Dictations(Seq(dictation)).right))

      dictationRegistrer ! DeleteDictation(UUID.randomUUID())
      expectMsgType[MissingDictation]
    }
  }

  private def createRandomDictation(name: String = s"ouhhh la la!!${UUID.randomUUID.toString}"): Dictation = {
    val dictation = DictationInput(
      name = name,
      entries = Seq("word 1")
    )

    dictationRegistrer ! CreateDictation(dictation)
    val success = expectMsgType[Success]

    Dictation(success.id, dictation.name, dictation.entries)
  }
}
