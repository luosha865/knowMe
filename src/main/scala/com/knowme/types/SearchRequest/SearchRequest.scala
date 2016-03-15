package com.knowme.types.SearchRequest

/**
  * Created by admin on 16/3/11.
  */
case class SearchRequest(text: String, rankOptions: RankOptions = null,
                         tokens: Array[String] = Array[String](),
                         labels: Array[String] = Array[String](),
                         docsIds: Map[Int,Boolean] = null, // null表示没有限制
                         timeOut: Int = 0,
                         countDocsOnly: Boolean = false,
                         orderless: Boolean = false)
