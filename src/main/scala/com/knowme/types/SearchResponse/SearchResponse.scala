package com.knowme.types.SearchResponse

/**
  * Created by admin on 16/2/22.
  */
case class SearchResponse(tokens: Array[String], docs: Array[ScoredDocument], timeout: Boolean = false, numDocs: Int = 0)
