package com.knowme.core.Ranker

/**
  * Created by admin on 16/2/19.
  */

import scala.collection.mutable._
import com.knowme.types.Index.IndexedDocument
import com.knowme.types.SearchRequest.RankOptions
import com.knowme.types.SearchResponse.ScoredDocument
import com.knowme.types.SearchResponse.ScoredDocuments
import java.util.concurrent.locks.ReentrantReadWriteLock

class Ranker {

  object lock {
    val fields: Map[Int, Any] = Map[Int, Any]()
    val docs: Map[Int, Boolean] = Map[Int, Boolean]()
    val lock: ReentrantReadWriteLock = new ReentrantReadWriteLock(true)
  }

  def AddDoc(docId: Int, fields : Any): Unit ={
    lock.lock.writeLock().lock()
    lock.fields += (docId -> fields)
    lock.docs += (docId -> true)
    lock.lock.writeLock().unlock()
  }

  def RemoveDoc(docId: Int): Unit ={
    lock.lock.writeLock().lock()
    lock.fields -= docId
    lock.docs -= docId
    lock.lock.writeLock().unlock()
  }

  def Rank(docs: Array[IndexedDocument], options: RankOptions, countDocsOnly: Boolean): (ScoredDocuments,Int) ={
    var numDocs = 0
    val outdocs: ArrayBuffer[ScoredDocument] = ArrayBuffer[ScoredDocument]()
    for(d<- docs) {
      lock.lock.readLock().lock()
      if(lock.docs.contains(d.docId)){
        val fs = lock.fields.get(d.docId)
        lock.lock.readLock().unlock()
        val scores = options.scoringCriteria.Score(d, fs.get)
        if(scores.length > 0){
          if(!countDocsOnly){
            outdocs.append(ScoredDocument(d.docId, scores, d.tokenSnippetLocations, d.tokenLocations))
          }
          numDocs += 1
        }
      }else{
        lock.lock.readLock().unlock()
      }
    }
    val outputDocs = ScoredDocuments(outdocs.toArray)
    if(!countDocsOnly){
      if(options.reverseOrder){
        outputDocs.sort(true)
      }else{
        outputDocs.sort(false)
      }
      val (start,end) = if(options.maxOutputs != 0){
        val s = options.outputOffset.min(outputDocs.length())
        val e =(options.outputOffset+options.maxOutputs).min(outputDocs.length())
        (s,e)
      } else {
        val s = options.outputOffset.min(outputDocs.length())
        val e = outputDocs.length()
        (s,e)
      }
      return (ScoredDocuments(outputDocs.docs.slice(start,end)),numDocs)
    }
    (outputDocs,numDocs)
  }
}


object Ranker{
  def apply(): Ranker ={
    new Ranker()
  }
}