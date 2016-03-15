package com.knowme.core.Indexer

/**
  * Created by admin on 16/2/24.
  */

import com.knowme.core.Indexer.Indexer
import com.knowme.types.IndexerInitOptions.{BM25Parameters, IndexerInitOptions, IndexType}
import com.knowme.types.Index.{KeywordIndex, DocumentIndex}
import scala.collection.mutable._

class IndexerTest {}

object IndexerTest{

  def TestAddKeywords(): Unit = {
    val indexer = Indexer(IndexerInitOptions(IndexType.LocationsIndex))
    indexer.AddDocument(DocumentIndex(1, 0.0, Array[KeywordIndex](KeywordIndex("token1", 0, Array[Int]()))))
    indexer.AddDocument(DocumentIndex(7, 0.0, Array[KeywordIndex](KeywordIndex("token1", 0, Array[Int]()))))
    indexer.AddDocument(DocumentIndex(2, 0.0, Array[KeywordIndex](KeywordIndex("token1", 0, Array[Int]()))))
    indexer.AddDocument(DocumentIndex(3, 0.0, Array[KeywordIndex](KeywordIndex("token2", 0, Array[Int]()))))
    indexer.AddDocument(DocumentIndex(1, 0.0, Array[KeywordIndex](KeywordIndex("token1", 0, Array[Int]()))))
    indexer.AddDocument(DocumentIndex(1, 0.0, Array[KeywordIndex](KeywordIndex("token2", 0, Array[Int]()))))
    indexer.AddDocument(DocumentIndex(2, 0.0, Array[KeywordIndex](KeywordIndex("token2", 0, Array[Int]()))))
    indexer.AddDocument(DocumentIndex(0, 0.0, Array[KeywordIndex](KeywordIndex("token2", 0, Array[Int]()))))
    println(indexer.indicesToString("token1"))
    println(indexer.indicesToString("token2"))
  }

  def TestLookup(): Unit ={
    val indexer = Indexer(IndexerInitOptions(IndexType.LocationsIndex))
    //doc0 = "token2 token3"
    indexer.AddDocument(DocumentIndex(0, 0.0, Array[KeywordIndex](
      KeywordIndex("token2", 0, Array[Int](0)),
      KeywordIndex("token3", 0, Array[Int](7))
    )))
    //doc1 = "token1 token2 token3"
    indexer.AddDocument(DocumentIndex(1, 0.0, Array[KeywordIndex](
      KeywordIndex("token1", 0, Array[Int](0)),
      KeywordIndex("token2", 0, Array[Int](7)),
      KeywordIndex("token3", 0, Array[Int](14))
    )))
    // doc2 = "token1 token2"
    indexer.AddDocument(DocumentIndex(2, 0.0, Array[KeywordIndex](
      KeywordIndex("token1", 0, Array[Int](0)),
      KeywordIndex("token2", 0, Array[Int](7))
    )))
    // doc3 = "token2"
    indexer.AddDocument(DocumentIndex(3, 0.0, Array[KeywordIndex](
      KeywordIndex("token2", 0, Array[Int](0))
    )))
    // doc7 = "token1 token3"
    indexer.AddDocument(DocumentIndex(7, 0.0, Array[KeywordIndex](
      KeywordIndex("token1", 0, Array[Int](0)),
      KeywordIndex("token3", 0, Array[Int](7))
    )))
    // doc9 = "token3"
    indexer.AddDocument(DocumentIndex(9, 0.0, Array[KeywordIndex](
      KeywordIndex("token3", 0, Array[Int](0))
    )))
    println(indexer.indicesToString("token1"))
    println(indexer.indicesToString("token2"))
    println(indexer.indicesToString("token3"))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token4"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token1"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token1","token4"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token1","token2"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token2","token1"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token1","token3"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token3","token1"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token2","token3"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token3","token2"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token1","token2","token3"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token3","token2","token1"), Array[String](), null, false)))
  }

  def TestDocIdsIndex(): Unit ={
    val indexer = Indexer(IndexerInitOptions(IndexType.DocIdsIndex))
    //doc0 = "token2 token3"
    indexer.AddDocument(DocumentIndex(0, 0.0, Array[KeywordIndex](
      KeywordIndex("token2", 0, Array[Int](0)),
      KeywordIndex("token3", 0, Array[Int](7))
    )))
    //doc1 = "token1 token2 token3"
    indexer.AddDocument(DocumentIndex(1, 0.0, Array[KeywordIndex](
      KeywordIndex("token1", 0, Array[Int](0)),
      KeywordIndex("token2", 0, Array[Int](7)),
      KeywordIndex("token3", 0, Array[Int](14))
    )))
    // doc2 = "token1 token2"
    indexer.AddDocument(DocumentIndex(2, 0.0, Array[KeywordIndex](
      KeywordIndex("token1", 0, Array[Int](0)),
      KeywordIndex("token2", 0, Array[Int](7))
    )))
    // doc3 = "token2"
    indexer.AddDocument(DocumentIndex(3, 0.0, Array[KeywordIndex](
      KeywordIndex("token2", 0, Array[Int](0))
    )))
    // doc7 = "token1 token3"
    indexer.AddDocument(DocumentIndex(7, 0.0, Array[KeywordIndex](
      KeywordIndex("token1", 0, Array[Int](0)),
      KeywordIndex("token3", 0, Array[Int](7))
    )))
    // doc9 = "token3"
    indexer.AddDocument(DocumentIndex(9, 0.0, Array[KeywordIndex](
      KeywordIndex("token3", 0, Array[Int](0))
    )))

    println(indexer.indicesToString("token1"))
    println(indexer.indicesToString("token2"))
    println(indexer.indicesToString("token3"))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token4"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token1"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token1","token4"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token1","token2"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token2","token1"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token1","token3"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token3","token1"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token2","token3"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token3","token2"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token1","token2","token3"), Array[String](), null, false)))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token3","token2","token1"), Array[String](), null, false)))
  }

  def TestLookupWithProximity(): Unit ={
    val indexer = Indexer(IndexerInitOptions(IndexType.LocationsIndex))
    // doc0 = "token2 token4 token4 token2 token3 token4"
    indexer.AddDocument(DocumentIndex(0, 0.0, Array[KeywordIndex](
      KeywordIndex("token2", 0, Array[Int](0,21)),
      KeywordIndex("token3", 0, Array[Int](28)),
      KeywordIndex("token4", 0, Array[Int](7,14,35))
    )))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token2", "token3"), Array[String](), null, false)))

    // doc0 = "t2 t1 . . . t2 t3"
    indexer.AddDocument(DocumentIndex(0, 0.0, Array[KeywordIndex](
      KeywordIndex("t1", 0, Array[Int](3)),
      KeywordIndex("t2", 0, Array[Int](0,12)),
      KeywordIndex("t3", 0, Array[Int](15))
    )))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("t1", "t2", "t3"), Array[String](), null, false)))

    // doc0 = "t3 t2 t1 . . . . . t2 t3"
    indexer.AddDocument(DocumentIndex(0, 0.0, Array[KeywordIndex](
      KeywordIndex("t1", 0, Array[Int](6)),
      KeywordIndex("t2", 0, Array[Int](3,19)),
      KeywordIndex("t3", 0, Array[Int](0,22))
    )))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("t1", "t2", "t3"), Array[String](), null, false)))

  }

  def TestLookupWithPartialLocations(): Unit ={
    val indexer = Indexer(IndexerInitOptions(IndexType.LocationsIndex))
    // doc0 = "token2 token4 token4 token2 token3 token4" + "label1"(不在文本中)
    indexer.AddDocument(DocumentIndex(0, 0.0, Array[KeywordIndex](
      KeywordIndex("token2", 0, Array[Int](0,21)),
      KeywordIndex("token3", 0, Array[Int](28)),
      KeywordIndex("label1", 0, Array[Int]()),
      KeywordIndex("token4", 0, Array[Int](7,14,35))
    )))
    // doc1 = "token2 token4 token4 token2 token3 token4"
    indexer.AddDocument(DocumentIndex(1, 0.0, Array[KeywordIndex](
      KeywordIndex("token2", 0, Array[Int](0,21)),
      KeywordIndex("token3", 0, Array[Int](28)),
      KeywordIndex("token4", 0, Array[Int](7,14,35))
    )))

    println(indexer.indicesToString("label1"))
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token2","token3"),Array[String]("label1"),null,false)))
  }

  def TestLookupWithBM25(): Unit ={
    val indexer = Indexer(IndexerInitOptions(IndexType.FrequenciesIndex,BM25Parameters(1.0,1.0)))
    // doc0 = "token2 token4 token4 token2 token3 token4"
    indexer.AddDocument(DocumentIndex(0, 6.0, Array[KeywordIndex](
      KeywordIndex("token2", 3.0, Array[Int](0,21)),
      KeywordIndex("token3", 7.0, Array[Int](28)),
      KeywordIndex("token4", 15.0, Array[Int](7,14,35))
    )))
    // doc1 = "token6 token7"
    indexer.AddDocument(DocumentIndex(1, 2.0, Array[KeywordIndex](
      KeywordIndex("token6", 3.0, Array[Int](0)),
      KeywordIndex("token7", 15.0, Array[Int](7))
    )))
    val (outputs, _) = indexer.Lookup(Array[String]("token2", "token3", "token4"), Array[String](), null, false)

    // BM25 = log2(3) * (12/9 + 28/17 + 60/33) = 6.3433
    println((outputs(0).bm25*10000).toInt.toString)
  }

  def TestLookupWithinDocIds(): Unit ={
    val indexer = Indexer(IndexerInitOptions(IndexType.LocationsIndex))
    // doc0 = "token2 token3"
    indexer.AddDocument(DocumentIndex(0, 0.0, Array[KeywordIndex](
      KeywordIndex("token2", 0, Array[Int](0)),
      KeywordIndex("token3", 0, Array[Int](7))
    )))

    // doc1 = "token1 token2 token3"
    indexer.AddDocument(DocumentIndex(1, 0.0, Array[KeywordIndex](
      KeywordIndex("token1", 0, Array[Int](0)),
      KeywordIndex("token2", 0, Array[Int](7)),
      KeywordIndex("token3", 0, Array[Int](14))
    )))

    // doc2 = "token1 token2"
    indexer.AddDocument(DocumentIndex(2, 0.0, Array[KeywordIndex](
      KeywordIndex("token1", 0, Array[Int](0)),
      KeywordIndex("token2", 0, Array[Int](7))
    )))

    // doc3 = "token2"
    indexer.AddDocument(DocumentIndex(3, 0.0, Array[KeywordIndex](
      KeywordIndex("token2", 0, Array[Int](0))
    )))

    val docIds = Map[Int,Boolean](0->true,2->true)
    println(Indexer.indexedDocsToString(indexer.Lookup(Array[String]("token2"),Array[String](),docIds,false)))
  }

  def TestLookupWithLocations(): Unit ={
    val indexer = Indexer(IndexerInitOptions(IndexType.LocationsIndex))
    // doc0 = "token2 token4 token4 token2 token3 token4"
    indexer.AddDocument(DocumentIndex(0, 0, Array[KeywordIndex](
      KeywordIndex("token2", 0, Array[Int](0,21)),
      KeywordIndex("token3", 0, Array[Int](28)),
      KeywordIndex("token4", 0, Array[Int](7,14,35))
    )))
    val (docs, _) = indexer.Lookup(Array[String]("token2", "token3"), Array[String](), null, false)
    println(docs(0).tokenLocations.map(_.toSeq).toSeq.toString)

  }

  def main(args: Array[String]): Unit = {
    //TestAddKeywords()
    //TestLookup()
    //TestDocIdsIndex()
    //TestLookupWithProximity()
    //TestLookupWithPartialLocations()
    //TestLookupWithBM25()
    //TestLookupWithinDocIds()
    //TestLookupWithLocations()
  }
}
