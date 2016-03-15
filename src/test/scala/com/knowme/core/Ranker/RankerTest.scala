package com.knowme.core.Ranker

/**
  * Created by admin on 16/2/24.
  */

import scala.collection.mutable._
import com.knowme.types.Index.IndexedDocument
import com.knowme.types.SearchRequest.RankOptions
import com.knowme.types.ScoringCriteria.RankByBM25

class RankerTest{}

object RankerTest{

  def TestRankDocument(): Unit ={
    val ranker: Ranker = Ranker()
    ranker.AddDoc(1,DummyScoringFields())
    ranker.AddDoc(3,DummyScoringFields())
    ranker.AddDoc(4,DummyScoringFields())
    val docs = Array[IndexedDocument](IndexedDocument(1, 6), IndexedDocument(3, 24), IndexedDocument(4, 18))
    val (scoredDocs, _) = ranker.Rank(docs, RankOptions(new RankByBM25(), false, 0, 0), false)
    println(scoredDocs.toString)

    val docs2 = Array[IndexedDocument](IndexedDocument(1, 6), IndexedDocument(3, 24), IndexedDocument(2, 0), IndexedDocument(4, 18))
    val (scoredDocs2, _) = ranker.Rank(docs2, RankOptions(new RankByBM25(), true, 0, 0), false)
    println(scoredDocs2.toString)
  }

  def TestRankWithCriteria(): Unit ={
    val ranker: Ranker = Ranker()
    ranker.AddDoc(1, DummyScoringFields("label3", 3, 22.3))
    ranker.AddDoc(2, DummyScoringFields("label4", 1, 2))
    ranker.AddDoc(3, DummyScoringFields("label1", 7, 10.3))
    ranker.AddDoc(4, DummyScoringFields("label1", -1, 3.3))
    val docs = Array[IndexedDocument](IndexedDocument(1, 6), IndexedDocument(2, -1), IndexedDocument(3, 24), IndexedDocument(4, 18))
    val (scoredDocs, _) = ranker.Rank(docs, RankOptions(new DummyScoringCriteria()), false)
    println(scoredDocs.toString)
    val (scoredDocs2, _) = ranker.Rank(docs, RankOptions(new DummyScoringCriteria(4)), false)
    println(scoredDocs2.toString)
  }

  def TestRemoveDocument() : Unit ={
    val ranker: Ranker = Ranker()
    ranker.AddDoc(1, DummyScoringFields("label3", 3, 22.3))
    ranker.AddDoc(2, DummyScoringFields("label4", 1, 2))
    ranker.AddDoc(3, DummyScoringFields("label1", 7, 10.3))
    ranker.RemoveDoc(3)
    val docs = Array[IndexedDocument](IndexedDocument(1, 6), IndexedDocument(2, -1), IndexedDocument(3, 24), IndexedDocument(4, 18))
    val (scoredDocs, _) = ranker.Rank(docs, RankOptions(new DummyScoringCriteria()), false)
    println(scoredDocs.toString)
  }


  def main(args: Array[String]) {
    TestRankDocument()
    TestRankWithCriteria()
    TestRemoveDocument()
  }
}
