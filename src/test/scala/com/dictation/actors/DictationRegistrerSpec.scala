package com.dictation.actors

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import com.dictation.actors.DictationRegistrer.{GetDictations, MissingDictation, Success, UpdateDictation}
import com.dictation.models.Models.{Dictation, DictationInput, Dictations}
import org.scalatest.{Matchers, WordSpecLike}

import scala.language.postfixOps

class DictationRegistrerSpec extends TestKit(ActorSystem()) with ImplicitSender with WordSpecLike with Matchers {

  val dictationregistrer: ActorRef =
    system.actorOf(DictationRegistrer.props, "dictationRegistry")

  "Fetching dictations" should {
    "return the existing dictations" in {
      dictationregistrer ! GetDictations
      val didactions = expectMsgType[Dictations]
      didactions should be(Dictations(Seq(
        Dictation(UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1449"), "Les Animaux", Seq("aaa", "bbb")),
        Dictation(UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1450"), "Les Arbres", Seq("aaa", "bbb")))
      ))
    }

    "return the existing dictations ordered by didaction id (GET /dictations)" in {
      // todo create stuff and make sure they are return ordered
    }
  }

  "Updating didactions" should {
    "properly update an existing dictation" in {
      val updatedDidaction = Dictation(
        id = UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1449"),
        name = "ouhhh la la!!",
        entries = Seq("word 1")
      )

      dictationregistrer ! UpdateDictation(updatedDidaction.id, DictationInput(updatedDidaction.name, updatedDidaction.entries))
      expectMsg(Success)

      dictationregistrer ! GetDictations
      val didactions = expectMsgType[Dictations]
      didactions should be(Dictations(Seq(
        updatedDidaction,
        Dictation(UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1450"), "Les Arbres", Seq("aaa", "bbb")))
      ))
    }

    "return a proper error when updating an non-existent dictation" in {
      val updatedDidaction = Dictation(
        id = UUID.fromString("a8bc70f4-b432-11e8-96f8-529269fb1400"),
        name = "ouhhh la la!!",
        entries = Seq.empty
      )

      dictationregistrer ! UpdateDictation(updatedDidaction.id, DictationInput(updatedDidaction.name, updatedDidaction.entries))
      expectMsgType[MissingDictation]
    }
  }
}
