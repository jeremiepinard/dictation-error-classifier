package com.dictation.actors

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import com.dictation.actors.DictationRegistrer._
import com.dictation.models.Models.{Dictation, DictationInput, Dictations}
import org.scalatest.{Matchers, WordSpecLike}

import scala.language.postfixOps

class DictationRegistrerSpec extends TestKit(ActorSystem()) with ImplicitSender with WordSpecLike with Matchers {

  val dictationregistrer: ActorRef =
    system.actorOf(DictationRegistrer.props, "dictationRegistry")

  "Fetching dictations" should {
    "return the existing dictations" in {
      val dictation1 = createRandomDictation()
      val dictation2 = createRandomDictation()

      dictationregistrer ! GetDictations
      val didactions = expectMsgType[Dictations]
      didactions.dictations.size should be(2)
      didactions.dictations should contain(Dictation(dictation1.id, dictation1.name, dictation1.entries))
      didactions.dictations should contain(Dictation(dictation2.id, dictation2.name, dictation2.entries))
    }

    "return the existing dictations ordered by name (GET /dictations)" in {
      val dictation1 = createRandomDictation(name = "AAAA")
      val dictation2 = createRandomDictation(name = "CCCC")
      val dictation3 = createRandomDictation(name = "BBB")

      dictationregistrer ! GetDictations
      val didactions = expectMsgType[Dictations]
      didactions.dictations.size should be(3)
      didactions.dictations(0).name should be("AAAA")
      didactions.dictations(1).name should be("BBB")
      didactions.dictations(2).name should be("CCCC")
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

      dictationregistrer ! UpdateDictation(updatedDidaction.id, DictationInput(updatedDidaction.name, updatedDidaction.entries))
      expectMsgType[Success]

      dictationregistrer ! GetDictations
      val didactions = expectMsgType[Dictations]
      didactions should be(Dictations(Seq(updatedDidaction)))
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

  "Deleting didactions" should {
    "properly delete an existing dictation" in {
      val didaction = createRandomDictation()

      dictationregistrer ! GetDictations
      val didactions1 = expectMsgType[Dictations]
      didactions1.dictations.size should be(1)

      dictationregistrer ! DeleteDictation(didaction.id)
      expectMsgType[Success]

      dictationregistrer ! GetDictations
      val didactions2 = expectMsgType[Dictations]
      didactions2.dictations.size should be(0)
    }

    "return a proper error when deleting an non-existent dictation" in {
      val didaction = createRandomDictation()

      dictationregistrer ! GetDictations
      val didactions1 = expectMsgType[Dictations]
      didactions1.dictations.size should be(1)

      dictationregistrer ! DeleteDictation(UUID.randomUUID())
      expectMsgType[MissingDictation]
    }
  }

  private def createRandomDictation(name: String = "ouhhh la la!!"): Dictation = {
    val dictation = DictationInput(
      name = name,
      entries = Seq("word 1")
    )

    dictationregistrer ! CreateDictation(dictation)
    val success = expectMsgType[Success]

    Dictation(success.id, dictation.name, dictation.entries)
  }
}
