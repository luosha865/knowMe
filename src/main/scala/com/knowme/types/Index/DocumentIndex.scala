package com.knowme.types.Index

/**
  * Created by admin on 16/2/19.
  */

// 文本的DocId   文本的关键词长   加入的索引键
case class DocumentIndex(docId: Int,tokenLength: Double, keywords : Array[KeywordIndex])