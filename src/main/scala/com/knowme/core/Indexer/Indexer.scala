package com.knowme.core.Indexer

import java.util.concurrent.locks.ReentrantReadWriteLock

import com.knowme.types.IndexerInitOptions.IndexerInitOptions

/**
  * Created by admin on 16/2/17.
  */

import scala.collection.mutable._
import com.knowme.types.IndexerInitOptions.IndexType
import com.knowme.types.Index.DocumentIndex
import com.knowme.types.Index.IndexedDocument
import scala.util.control.Breaks._

class Indexer(initOptions: IndexerInitOptions) {

  object tableLock {
    val table: Map[String, KeywordIndices] = Map[String, KeywordIndices]()
    val docs: Map[Int, Boolean] = Map[Int, Boolean]()
    val lock: ReentrantReadWriteLock = new ReentrantReadWriteLock(true)
  }

  var numDocuments: Int = 0
  var totalTokenLength: Double = 0.0
  val docTokenLengths: Map[Int, Double] = Map[Int, Double]()

  def AddDocument(document: DocumentIndex) = {
    tableLock.lock.writeLock().lock()
    try {
      if (document.tokenLength != 0) {
        val originalLength = docTokenLengths.get(document.docId)
        docTokenLengths += (document.docId -> document.tokenLength)
        totalTokenLength += document.tokenLength - originalLength.getOrElse(0.0)
      }

      var docIdIsNew = true
      for (keyword <- document.keywords) {
        val indices = tableLock.table.get(keyword.text).getOrElse(null)
        if (indices == null) {
          val ti: KeywordIndices = initOptions.indexType match {
            case IndexType.LocationsIndex => {
              KeywordIndices(ArrayBuffer[Int](document.docId), ArrayBuffer[Double](), ArrayBuffer[Array[Int]](keyword.starts))
            }
            case IndexType.FrequenciesIndex => {
              KeywordIndices(ArrayBuffer[Int](document.docId), ArrayBuffer[Double](keyword.frequency))
            }
            case _ : IndexType.Value => {
              KeywordIndices(ArrayBuffer[Int](document.docId))
            }
          }
          tableLock.table += (keyword.text -> ti)
        } else {
          val (position, found) = searchIndex(indices, 0, getIndexLength(indices) - 1, document.docId)
          if (found) {
            docIdIsNew = false
            // 覆盖已有的索引项
            initOptions.indexType match {
              case IndexType.LocationsIndex => {
                indices.locations.update(position, keyword.starts)
              }
              case IndexType.FrequenciesIndex => {
                indices.frequencies.update(position, keyword.frequency)
              }
              case _ : IndexType.Value => {}
            }
          } else {
            initOptions.indexType match {
              case IndexType.LocationsIndex => {
                indices.locations.insert(position, keyword.starts)
              }
              case IndexType.FrequenciesIndex => {
                indices.frequencies.insert(position, keyword.frequency)
              }
              case _ : IndexType.Value => {}
            }
            indices.docIds.insert(position, document.docId)
          }
        }
      }
      if (docIdIsNew) {
        tableLock.docs += (document.docId -> true)
        numDocuments += 1
      }
    } catch {
      case e : Throwable => println(e.toString)
    } finally {
      tableLock.lock.writeLock().unlock()
    }
  }

  def Lookup(tokens: Array[String], labels: Array[String], docIds: scala.collection.Map[Int, Boolean], countDocsOnly: Boolean): (Array[IndexedDocument], Int) = {
    val docs = ArrayBuffer[IndexedDocument]()
    if (numDocuments == 0) {
      return (Array[IndexedDocument](), 0)
    }
    var numDocs: Int = 0
    val keywords: Array[String] = tokens ++ labels //new Array[String](tokens.length+labels.length)
    tableLock.lock.readLock().lock()
    try {
      // 当反向索引表中无此搜索键时直接返回
      // 否则加入反向表中
      val table = keywords.map( tableLock.table.get(_).getOrElse(null) )
      if(table.contains(null)){
        return (docs.toArray, numDocuments)
      }
      // 当没有找到时直接返回
      if (table.length == 0) {
        return (docs.toArray, numDocuments)
      }
      // 归并查找各个搜索键出现文档的交集
      // 从后向前查保证先输出DocId较大文档
      val indexPointers = table.map(getIndexLength(_) - 1)
      // 平均文本关键词长度，用于计算BM25
      val avgDocLength = totalTokenLength / numDocuments.toDouble

      breakable {
        while (indexPointers(0) >= 0) {
          // 以第一个搜索键出现的文档作为基准，并遍历其他搜索键搜索同一文档
          val baseDocId = getDocId(table(0), indexPointers(0))
          var flag = true
          if (docIds != null) {
            val found = docIds.get(baseDocId)
            if (found.isEmpty) {
              flag = false
            }
          }
          if (flag) {
            var found = true
            var exit = false
            breakable {
              for (iTable <- table.indices) {
                // 二分法比简单的顺序归并效率高，也有更高效率的算法，
                // 但顺序归并也许是更好的选择，考虑到将来需要用链表重新实现
                // 以避免反向表添加新文档时的写锁。
                // TODO: 进一步研究不同求交集算法的速度和可扩展性。
                val (position, foundBaseDocId) = searchIndex(table(iTable), 0, indexPointers(iTable), baseDocId)
                if (foundBaseDocId) {
                  indexPointers(iTable) = position
                } else {
                  found = false
                  if (position == 0) {
                    // 该搜索键中所有的文档ID都比baseDocId大，因此已经没有
                    // 继续查找的必要。
                    exit = true
                  } else {
                    // 继续下一indexPointers[0]的查找
                    indexPointers(iTable) = position - 1
                  }
                  break
                }
              }
            }
            if(exit) {
              break
            }
            if (found) {
              val ok = tableLock.docs.get(baseDocId)
              if (!ok.isEmpty) {
                val indexedDoc: IndexedDocument = new IndexedDocument()
                // 当为LocationsIndex时计算关键词紧邻距离
                if (initOptions.indexType == IndexType.LocationsIndex) {
                  // 计算有多少关键词是带有距离信息的
                  var numTokensWithLocations = 0
                  for (i <- table.take(tokens.length).indices) {
                    val t = table.apply(i)
                    if (t.locations(indexPointers(i)).length > 0) {
                      numTokensWithLocations += 1
                    }
                  }
                  if (numTokensWithLocations != tokens.length) {
                    if (!countDocsOnly) {
                      val tmp = new IndexedDocument()
                      tmp.docId = baseDocId
                      docs.append(tmp)
                    }
                    numDocs += 1
                    break
                  }
                  // 计算搜索键在文档中的紧邻距离
                  val (tokenProximity, tokenLocations) = computeTokenProximity(table.take(tokens.length), indexPointers, tokens)
                  indexedDoc.tokenProximity = tokenProximity
                  indexedDoc.tokenSnippetLocations = tokenLocations
                  indexedDoc.tokenLocations = new Array[Array[Int]](tokens.length)
                  // 添加TokenLocations
                  for (i <- table.take(tokens.length).indices) {
                    val t = table(i)
                    indexedDoc.tokenLocations(i) = t.locations(indexPointers(i))
                  }
                }
                // 当为LocationsIndex或者FrequenciesIndex时计算BM25
                if (initOptions.indexType == IndexType.LocationsIndex || initOptions.indexType == IndexType.FrequenciesIndex) {
                  var bm25: Double = 0.0
                  val d = docTokenLengths.get(baseDocId).getOrElse(0.0)
                  for (i <- table.take(tokens.length).indices) {
                    val t = table(i)
                    val frequency: Double = if (initOptions.indexType == IndexType.LocationsIndex) {
                      t.locations(indexPointers(i)).length.toDouble
                    } else {
                      t.frequencies(indexPointers(i))
                    }
                    if (t.docIds.length > 0 && frequency > 0 && initOptions.bM25Parameters != null && avgDocLength != 0) {
                      def log2(x: Double): Double = scala.math.log(x) / scala.math.log(2)
                      val idf = log2(numDocuments.toDouble / t.docIds.length.toDouble + 1.0)
                      val k1 = initOptions.bM25Parameters.k1
                      val b = initOptions.bM25Parameters.b
                      bm25 += idf * frequency * (k1 + 1) / (frequency + k1 * (1 - b + b * d / avgDocLength))
                    }
                  }
                  indexedDoc.bm25 = bm25
                }
                indexedDoc.docId = baseDocId
                if (!countDocsOnly) {
                  docs.append(indexedDoc)
                }
                numDocs += 1
              }
            }
          }
          indexPointers(0) -= 1
        }
      }
    } catch {
      case e: Throwable => println(e.toString)
    } finally {
      tableLock.lock.readLock().unlock()
    }
    (docs.toArray, numDocs)
  }

  def searchIndex(indices: KeywordIndices, start: Int, end: Int, docId: Int): (Int, Boolean) = {
    if (getIndexLength(indices) == start) {
      return (start, false)
    }
    if (docId < getDocId(indices, start)) {
      return (start, false)
    } else if (docId == getDocId(indices, start)) {
      return (start, true)
    }
    if (docId > getDocId(indices, end)) {
      return (end + 1, false)
    } else if (docId == getDocId(indices, end)) {
      return (end, true)
    }
    // 二分
    var s: Int = start
    var e: Int = end
    var middle: Int = (s + e) / 2
    while (e - s > 1) {
      middle = (s + e) / 2
      if (docId == getDocId(indices, middle)) {
        return (middle, true)
      } else if (docId > getDocId(indices, middle)) {
        s = middle
      } else {
        e = middle
      }
    }
    (end, false)
  }


  // 计算搜索键在文本中的紧邻距离
  //
  // 假定第 i 个搜索键首字节出现在文本中的位置为 P_i，长度 L_i
  // 紧邻距离计算公式为
  //
  // 	ArgMin(Sum(Abs(P_(i+1) - P_i - L_i)))
  //
  // 具体由动态规划实现，依次计算前 i 个 token 在每个出现位置的最优值。
  // 选定的 P_i 通过 tokenLocations 参数传回。
  def computeTokenProximity(table: Array[KeywordIndices], indexPointers: Array[Int], tokens: Array[String]): (Int, Array[Int]) = {
    var minTokenProximity = -1
    val tokenLocations = new Array[Int](tokens.length)

    var currentLocations, nextLocations: Array[Int] = null
    var currentMinValues, nextMinValues: Array[Int] = null
    val path: Array[Array[Int]] = new Array[Array[Int]](tokens.length)

    for (i <- path.indices) {
      path(i) = new Array[Int](table(i).locations(indexPointers(i)).length) //make([]int, len(table[i].locations[indexPointers[i]]))
    }

    currentLocations = table(0).locations(indexPointers(0))
    currentMinValues = new Array[Int](currentLocations.length)
    for (i <- 1 until tokens.length) {
      nextLocations = table(i).locations(indexPointers(i))
      nextMinValues = new Array[Int](nextLocations.length)
      for (j <- nextMinValues.indices) {
        nextMinValues(j) = -1
      }
      var iNext: Int = 0
      for (iCurrent <- currentLocations.indices) {
        val currentLocation = currentLocations(iCurrent)
        var flag = true
        if (currentMinValues(iCurrent) == -1) {
          flag = false
        }
        if (flag) {
          while (iNext + 1 < nextLocations.length && nextLocations(iNext + 1) < currentLocation) {
            iNext += 1
          }
          def update(from: Int, to: Int): Unit = {
            if (to < nextLocations.length) {
              val value = currentMinValues.apply(from) + math.abs(nextLocations.apply(to) - currentLocations.apply(from) - tokens.apply(i - 1).length)
              if (nextMinValues(to) == -1 || value < nextMinValues(to)) {
                nextMinValues(to) = value
                path(i)(to) = from
              }
            }
          }
          // 最优解的状态转移只发生在左右最接近的位置
          update(iCurrent, iNext)
          update(iCurrent, iNext + 1)
        }
      }
      currentLocations = nextLocations
      currentMinValues = nextMinValues
    }
    var cursor: Int = 0
    for (i <- currentMinValues.indices) {
      var flag = true
      val value = currentMinValues(i)
      if (value == -1) {
        flag = false
      }
      if (flag) {
        if (minTokenProximity == -1 || value < minTokenProximity) {
          minTokenProximity = value
          cursor = i
        }
      }
    }

    // 从路径倒推出最优解的位置
    for (i <- tokens.indices.reverse) {
      if (i != tokens.length - 1) {
        cursor = path(i + 1)(cursor)
      }
      tokenLocations(i) = table(i).locations(indexPointers(i))(cursor)
    }
    (minTokenProximity, tokenLocations)
  }

  // 得到KeywordIndices中文档总数
  def getIndexLength(ti: KeywordIndices): Int = {
    ti.docIds.length
  }

  // 从KeywordIndices中得到第i个文档的DocId
  def getDocId(ti: KeywordIndices, i: Int): Int = {
    ti.docIds.apply(i)
  }

  def RemoveDoc(docId: Int) {
    tableLock.lock.writeLock().lock()
    tableLock.docs -= docId
    numDocuments -= 1
    tableLock.lock.writeLock().unlock()
  }

  def indicesToString(token: String): String = {
    val indices = tableLock.table.get(token)
    var output: String = ""
    if (!indices.isEmpty){
      for (i <- 0 until getIndexLength(indices.get)) {
        output += "%d ".format(getDocId(indices.get, i))
      }
    }
    output
  }
}

object Indexer{

  def indexedDocsToString(kv :(Array[IndexedDocument], Int)): String ={
    val docs = kv._1
    val numDocs = kv._2
    var output = ""
    for(doc <- docs) {
      val ts = if(doc.tokenSnippetLocations != null){
        "[" + doc.tokenSnippetLocations.map(_.toString).mkString(" ") +"]"
      }else{
        "[]"
      }
      output += "[%d %d %s] ".format(doc.docId, doc.tokenProximity,ts)
    }
    output
  }

  def apply(initOptions: IndexerInitOptions): Indexer ={
    new Indexer(initOptions)
  }
}
