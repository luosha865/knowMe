package com.knowme.engine

import com.knowme.types.DocumentIndexData.DocumentIndexData
import com.knowme.types.EngineInitOptions.EngineInitOptions
import com.knowme.types.Index.IndexedDocument
import com.knowme.types.IndexerInitOptions.{IndexType, IndexerInitOptions}
import com.knowme.types.ScoringCriteria.ScoringCriteria
import com.knowme.types.SearchRequest.{SearchRequest, RankOptions}

/**
  * Created by admin on 16/3/15.
  */
class EngineTest {

}

object EngineTest{

  case class ScoringFields(A:Double,B:Double,C:Double){}

  class RankByTokenProximity extends ScoringCriteria {
    def Score(doc: IndexedDocument,fields: Any): Array[Double]  = {
      if(doc.tokenProximity < 0) {
        Array[Double]()
      }else{
        Array[Double](1.0 / (doc.tokenProximity.toDouble + 1))
      }
    }
  }

  def AddDocs(engine: Engine): Unit ={
    engine.IndexDocument(0, DocumentIndexData(content = "中国有十三亿人口人口",fields = ScoringFields(1, 2, 3)))
    engine.IndexDocument(1,DocumentIndexData(content = "中国人口",fields = null))
    engine.IndexDocument(2, DocumentIndexData(content = "有人口",fields = ScoringFields(2, 3, 1)))
    engine.IndexDocument(3, DocumentIndexData(content = "有十三亿人口", fields = ScoringFields(2, 3, 3)))
    engine.IndexDocument(4, DocumentIndexData(content ="中国十三亿人口", fields = ScoringFields(0, 9, 1)))
    engine.FlushIndex()
  }


  def TestFun(): Unit ={
    val options = EngineInitOptions(
      defaultRankOptions = RankOptions(new RankByTokenProximity(),true,0,10),
      indexerInitOptions = IndexerInitOptions(IndexType.LocationsIndex)
    )
    val engine: Engine = Engine(options)
    AddDocs(engine)
    TestEngineIndexDocument(engine)
    TestReverseOrder(engine)
    TestOffsetAndMaxOutputs(engine)
  }

  def TestEngineIndexDocument(engine: Engine): Unit ={
    val outputs =engine.Search(SearchRequest(text="中国人口"))
    println(outputs.tokens.length)
    println(outputs.tokens.mkString(","))
    println(outputs.docs.length.toString)
    println(outputs.docs.map(_.docId.toString).mkString(","))
    println(outputs.docs.map(x => (x.scores(0)*1000).toInt).mkString(" "))
    println(outputs.docs.map(_.tokenSnippetLocations.mkString(",")).mkString(" "))
  }

  def TestReverseOrder(engine: Engine): Unit ={
    val outputs =engine.Search(SearchRequest(text="中国人口"))
    println(outputs.tokens.length)
    println(outputs.tokens.mkString(","))
    println(outputs.docs.length.toString)
    println(outputs.docs.map(_.docId.toString).mkString(","))
    println(outputs.docs.map(x => (x.scores(0)*1000).toInt).mkString(" "))
    println(outputs.docs.map(_.tokenSnippetLocations.mkString(",")).mkString(" "))
  }

  def TestOffsetAndMaxOutputs(engine: Engine): Unit ={
    val outputs =engine.Search(SearchRequest(text="中国人口",rankOptions = RankOptions(
      new RankByTokenProximity(),true,1,3)))
    println(outputs.tokens.length)
    println(outputs.tokens.mkString(","))
    println(outputs.docs.length.toString)
    println(outputs.docs.map(_.docId.toString).mkString(","))
    println(outputs.docs.map(x => (x.scores(0)*1000).toInt).mkString(" "))
    println(outputs.docs.map(_.tokenSnippetLocations.mkString(",")).mkString(" "))
  }

  def main(args: Array[String]): Unit = {
    TestFun()
  }
}