package com.knowme.types.IndexerInitOptions

/**
  * Created by admin on 16/2/17.
  */

case class IndexerInitOptions(indexType: IndexType.IndexType, bM25Parameters: BM25Parameters = BM25Parameters(2.0,0.75))
