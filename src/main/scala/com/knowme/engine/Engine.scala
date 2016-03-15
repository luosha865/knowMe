package com.knowme.engine


/**
  * Created by admin on 16/2/17.
  */

import java.util.concurrent.atomic.AtomicInteger

import com.knowme.types.DocumentIndexData.DocumentIndexData
import com.knowme.types.EngineInitOptions.EngineInitOptions
import com.knowme.core.Indexer.Indexer
import com.knowme.core.Ranker.Ranker
import com.knowme.analysis.{JiebaTokenizer, Tokenizer}
import com.knowme.storage.Storage
import com.knowme.types.EngineInitOptions.EngineInitOptions
import com.knowme.types.SearchRequest.{RankOptions, SearchRequest}
import com.knowme.types.SearchResponse.{ScoredDocuments, SearchResponse}
import com.knowme.engine.RankerWorker.{RankerAddDocRequest,RankerRankRequest,RankerRemoveDocRequest,RankerReturnRequest}
import com.knowme.engine.IndexerWorker.{IndexerAddDocumentRequest,IndexerLookupRequest,IndexerRemoveDocRequest}
import com.knowme.engine.SegmenterWorker.SegmenterRequest
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.{ ask, pipe }
import org.apache.logging.log4j.LogManager
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.util.Timeout
import scala.util.hashing.MurmurHash3


class Engine(options: EngineInitOptions = EngineInitOptions()){

  private[this] val logger = LogManager.getLogger(this.getClass.getName)

  val initOptions: EngineInitOptions = options
  val initialized: Boolean = true
  val indexers: Array[Indexer] = Array.fill(options.numShards)(Indexer(options.indexerInitOptions))
  val rankers: Array[Ranker] = Array.fill(options.numShards)(Ranker())
  val segmenter: Tokenizer = new JiebaTokenizer()
  val stopTokens: StopTokens = new StopTokens(options.stopTokenFile)
  val dbs: Array[Storage] = Array[Storage]()

  var numDocumentsIndexed: AtomicInteger = new AtomicInteger(0)
  var numTokenIndexAdded: AtomicInteger =  new AtomicInteger(0)
  var numIndexingRequests: AtomicInteger =  new AtomicInteger(0)
  var numDocumentsStored: AtomicInteger =  new AtomicInteger(0)

  val system = ActorSystem("KnowMeSystem")
  val segmenterWorker = system.actorOf(Props(new SegmenterWorker(this)), name = "segmenterWorker")
  val indexerWorkers = Array.tabulate(options.numShards)(
    x => system.actorOf(Props(new IndexerWorker(this,x)), name = "indexerWorker%d".format(x))
  )
  val rankerWorkers = Array.tabulate(options.numShards)(
    x => system.actorOf(Props(new RankerWorker(this,x)), name = "rankerWorker%d".format(x))
  )

  def getShard(hash: Int): Int = {
    //println(hash)
    //println(hash - hash / initOptions.numShards * initOptions.numShards)
    (hash - hash / initOptions.numShards * initOptions.numShards).toInt
  }

  def Search(request: SearchRequest) : SearchResponse = {

    val rankOptions: RankOptions = if(request.rankOptions == null){
      initOptions.defaultRankOptions
    }else{
      request.rankOptions
    }
    val tokens : Array[String] = if(request.text != ""){
      segmenter.analysis(request.text).map(_.token.text).filter(!stopTokens.isStopToken(_))
    }else{
      request.tokens
    }
    //生成查找请求
    val lookupRequest = IndexerLookupRequest(tokens,request.labels,request.docsIds,rankOptions,request.countDocsOnly,request.orderless)
    //向索引器发送查找请求
    //读取排序器的输出
    implicit val timeout = Timeout(100 seconds)
    //TODO need fix false
    val isTimeout : Boolean = false
    val rankerOutputs = indexerWorkers.map(_ ? lookupRequest).map({
      Await.result(_, timeout.duration).asInstanceOf[RankerReturnRequest]
    })

    val numDocs = rankerOutputs.map(_.numDocs).sum //reduce(_+_)

    val rankOutput = if(!request.countDocsOnly){
      rankerOutputs.map(_.docs).reduce(_++_)
    }else{
      ScoredDocuments()
    }

    // 再排序
    if(!request.countDocsOnly && !request.orderless){
      if(rankOptions.reverseOrder) {
        rankOutput.sort(true)
      } else {
        rankOutput.sort(false)
      }
    }
    // 准备输出
    //output.Tokens = tokens
    val docs = if(!request.countDocsOnly){
      if(request.orderless) {
        // 无序状态无需对Offset截断
        rankOutput.getDocs()
      } else {
        val (start, end) = if(rankOptions.maxOutputs == 0){
          val s = rankOptions.outputOffset.min(rankOutput.length())
          val e = rankOutput.length()
          (s,e)
        } else {
          val s = rankOptions.outputOffset.min(rankOutput.length())
          val e = (s+rankOptions.maxOutputs).min(rankOutput.length())
          (s,e)
        }
        rankOutput.slice(start,end).getDocs()
      }
    }else{
      null
    }
    SearchResponse(tokens,docs,isTimeout ,numDocs)
  }

  def RemoveDocument(docId: Int): Unit = {
    logger.info("RemoveDocumentt:%d".format(docId))
    indexerWorkers.foreach(_ ! IndexerRemoveDocRequest(docId))
    rankerWorkers.foreach(_ ! RankerRemoveDocRequest(docId))
    if(initOptions.usePersistentStorage){
      //TODO remove for dbs
    }
  }

  //这个函数调用是非同步的，也就是说在函数返回时有可能文档还没有加入索引中，因此
  //如果立刻调用Search可能无法查询到这个文档。强制刷新索引请调用FlushIndex函数。
  def IndexDocument(docId: Int, data: DocumentIndexData): Unit = {
    logger.info("IndexDocument:%d %s".format(docId,data.content.substring(0,20.min(data.content.length))))
    internalIndexDocument(docId,data)
    if(initOptions.usePersistentStorage){
      //TODO index for dbs
    }

  }

  def internalIndexDocument(docId: Int, data: DocumentIndexData): Unit = {
    numIndexingRequests.incrementAndGet()
    val hash: Int = MurmurHash3.stringHash("%d%s".format(docId,data.content)).abs
    segmenterWorker ! SegmenterRequest(docId,hash,data)
  }


  // 阻塞等待直到所有索引添加完毕
  def FlushIndex(): Unit = {
    //TODO wait untill finish
    while(numIndexingRequests.get != numDocumentsIndexed.get){
      //logger.info("numIndexingRequests:%d, numDocumentsIndexed:%d".format(numIndexingRequests.get,numDocumentsIndexed.get))
      Thread.`yield`()
    }
  }


  def Close(): Unit ={
    FlushIndex()
    if(initOptions.usePersistentStorage){
      //TODO close all dbs
      //dbs.map(_.Close())
    }
  }


}


object Engine{
  def apply(options: EngineInitOptions = EngineInitOptions()): Engine ={
    new Engine(options)
  }
}
