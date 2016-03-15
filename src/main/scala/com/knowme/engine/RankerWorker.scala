package com.knowme.engine

import akka.actor.Actor
import com.knowme.types.Index.IndexedDocument
import com.knowme.types.SearchRequest.RankOptions
import com.knowme.types.SearchResponse.ScoredDocuments
import org.apache.logging.log4j.LogManager

/**
  * Created by admin on 16/3/10.
  */
class RankerWorker (engine: Engine, shard: Int) extends Actor{

  import RankerWorker.{RankerAddDocRequest,RankerRankRequest,RankerReturnRequest,RankerRemoveDocRequest}
  private[this] val logger = LogManager.getLogger(this.getClass.getName + shard.toString)

  def receive = {
    case RankerAddDocRequest(docId, fields) => {
      logger.info("RankerAddDocRequest(%d)".format(docId))
      engine.rankers(shard).AddDoc(docId, fields)
    }
    case RankerRankRequest(docs,options,countDocsOnly)=> {
      logger.info("RankerRankRequest")
      val maxOutputs =
        if(options.maxOutputs !=0){
          options.maxOutputs + options.outputOffset
        }else{
          options.maxOutputs
        }
      val options_v2 =  RankOptions(options.scoringCriteria,options.reverseOrder,0,maxOutputs)
      val (outputDocs, numDocs) = engine.rankers(shard).Rank(docs, options_v2, countDocsOnly)
      sender ! RankerReturnRequest(outputDocs,numDocs)
      //request.rankerReturnChannel <- rankerReturnRequest{docs: outputDocs, numDocs: numDocs}
    }
    case RankerRemoveDocRequest(docId) => {
      logger.info("RankerRemoveDocRequest(%d)".format(docId))
      engine.rankers(shard).RemoveDoc(docId)
    }

  }

}


object RankerWorker{
  case class RankerAddDocRequest(docId: Int, fields: Any)
  case class RankerRankRequest(docs: Array[IndexedDocument], options: RankOptions,countDocsOnly: Boolean)
  case class RankerReturnRequest(docs: ScoredDocuments = ScoredDocuments(), numDocs: Int = 0)
  case class RankerRemoveDocRequest(docId: Int)

}
