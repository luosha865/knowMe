package com.knowme.types.Index

/**
  * Created by admin on 16/2/19.
  */

// 反向索引项，这实际上标注了一个（搜索键，文档）对。
// 搜索键的UTF-8文本  搜索键词频  搜索键在文档中的起始字节位置，按照升序排列
case class KeywordIndex(text: String, frequency: Double, starts: Array[Int])
