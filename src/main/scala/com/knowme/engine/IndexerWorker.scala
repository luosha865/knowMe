package com.knowme.engine

/**
  * Created by admin on 16/3/10.
  */

import com.knowme.types.Index.{IndexedDocument, DocumentIndex, KeywordIndex}
import com.knowme.types.SearchRequest.RankOptions
import akka.actor.Actor
import com.knowme.types.SearchResponse.{ScoredDocuments, ScoredDocument}
import akka.pattern.{ ask, pipe }
import org.apache.logging.log4j.LogManager
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.util.Timeout

class IndexerWorker  (engine: Engine, shard: Int) extends Actor{

  import IndexerWorker.{IndexerAddDocumentRequest,IndexerLookupRequest,IndexerRemoveDocRequest}
  import com.knowme.engine.RankerWorker.{RankerReturnRequest,RankerRankRequest}
  private[this] val logger = LogManager.getLogger(this.getClass.getName + shard.toString)


  def receive = {
    case IndexerAddDocumentRequest(document) => {
      logger.info("IndexerAddDocumentRequest(%d)".format(document.docId))
      engine.indexers(shard).AddDocument(document)
      engine.numTokenIndexAdded.addAndGet(document.keywords.length)
      val numDocumentsIndexed = engine.numDocumentsIndexed.incrementAndGet()
    }
    case IndexerLookupRequest(tokens,labels, docIds, options, countDocsOnly, orderless) =>{
      logger.info("IndexerLookupRequest:%s".format(tokens.mkString(",")))
      val (docs, numDocs) = if(docIds == null){
        engine.indexers(shard).Lookup(tokens, labels, null, countDocsOnly)
      }else{
        engine.indexers(shard).Lookup(tokens, labels, docIds, countDocsOnly)
      }
      if(countDocsOnly){
        sender ! RankerReturnRequest(null, numDocs)
      }else if(docs.length == 0){
        sender ! RankerReturnRequest()
      }else if(orderless){
        val outputDocs = docs.map({
          d => ScoredDocument(d.docId,null,d.tokenSnippetLocations,d.tokenLocations)
        })
        sender ! RankerReturnRequest(  ScoredDocuments(outputDocs), outputDocs.length)
      }else{
        val rankerRankRequest = RankerRankRequest(docs,options,countDocsOnly)
        implicit val timeout = Timeout(10 seconds)
        val future = engine.rankerWorkers(shard) ? rankerRankRequest
        val result = Await.result(future, timeout.duration).asInstanceOf[RankerReturnRequest]
        sender ! result
      }
    }
    case IndexerRemoveDocRequest(docId) => {
      logger.info("IndexerRemoveDocRequest(%d)".format(docId))
      engine.indexers(shard).RemoveDoc(docId)
    }

  }
}

object IndexerWorker{

  case class IndexerAddDocumentRequest(document: DocumentIndex)//, tokenLength: Double = 0.0 , keywords: Array[KeywordIndex]= Array[KeywordIndex]()
  case class IndexerLookupRequest(tokens: Array[String],labels: Array[String],
                                  docIds: Map[Int,Boolean] = null, options: RankOptions,
                                  countDocsOnly: Boolean = false, orderless: Boolean = false)
  case class IndexerRemoveDocRequest(docId: Int)

}