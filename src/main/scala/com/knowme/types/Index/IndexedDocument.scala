package com.knowme.types.Index

/**
  * Created by admin on 16/2/17.
  */


// 索引器返回结果
// BM25，仅当索引类型为FrequenciesIndex或者LocationsIndex时返回有效值。
// 关键词在文档中的紧邻距离，紧邻距离的含义见computeTokenProximity的注释。仅当索引类型为LocationsIndex时返回有效值。
// 紧邻距离计算得到的关键词位置，和Lookup函数输入tokens的长度一样且一一对应。仅当索引类型为LocationsIndex时返回有效值。
// 关键词在文本中的具体位置。 仅当索引类型为LocationsIndex时返回有效值。
class IndexedDocument {
  var docId: Int = 0
  var bm25: Double = 0.0
  var tokenProximity: Int = 0
  var tokenSnippetLocations: Array[Int] = null
  var tokenLocations: Array[Array[Int]] = null
}

object IndexedDocument{
  def apply(d: Int = 0, b: Double = 0.0 ,tp: Int = 0, ts: Array[Int] = null, tl: Array[Array[Int]] = null): IndexedDocument= {
    val doc = new IndexedDocument()
    doc.docId = d
    doc.bm25 = b
    doc.tokenProximity = tp
    doc.tokenSnippetLocations = ts
    doc.tokenLocations = tl
    doc
  }
}

