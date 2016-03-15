package com.knowme.engine

/**
  * Created by admin on 16/3/10.
  */

import akka.actor._
import com.knowme.engine.RankerWorker.RankerAddDocRequest
import com.knowme.types.DocumentIndexData.DocumentIndexData
import scala.collection.mutable._
import com.knowme.types.DocumentIndexData.TokenData
import com.knowme.engine.IndexerWorker.IndexerAddDocumentRequest
import com.knowme.types.Index.{DocumentIndex,KeywordIndex}
import org.apache.logging.log4j.LogManager

object SegmenterWorker{
  case class SegmenterRequest(docId: Int, hash: Int, data: DocumentIndexData)

}


class SegmenterWorker(engine: Engine) extends Actor{

  private[this] val logger = LogManager.getLogger(this.getClass.getName)

  def receive = {
    case SegmenterWorker.SegmenterRequest(docId, hash, data) => {
      logger.info("doc:%d hash:%d content:%s".format(docId,hash, data.content.substring(0,20.min(data.content.length))))

      val shard = engine.getShard(hash)
      val (tokensMap,numTokens) =
        if(engine.initOptions.usingSegmenter){
          val segments = engine.segmenter.analysis(data.content)
          val tm = (segments.map(segment => (segment.token.text,segment.start))
            ++ data.labels.map((_,-1))).filter({
              case (t,s) => !engine.stopTokens.isStopToken(t)}).groupBy(_._1).map({
              case (k:String,v:Array[(String,Int)]) => (k,v.map(_._2).filter(_>=0))
            })
            (tm,segments.length)
        }else{
          //tokens和labels 不相交
          val tm = (data.tokens.map({
              case TokenData(token, location) => (token,location)
            }).filter({
              case (t,s) => !engine.stopTokens.isStopToken(t)
            }) ++ data.labels.map((_,Array[Int]()))).toMap
          (tm,data.tokens.length)
        }
     val keywords = tokensMap.map({
        case (k,v) =>
          KeywordIndex(k,v.length.toDouble,v)
      }).toArray
      val indexerRequest:IndexerAddDocumentRequest = IndexerAddDocumentRequest(
        DocumentIndex(docId,numTokens.toDouble,keywords)
      )
      engine.indexerWorkers(shard) ! indexerRequest
      val rankerRequest: RankerAddDocRequest = RankerAddDocRequest(docId,data.fields)
      engine.rankerWorkers(shard) ! rankerRequest
    }
  }
}