package com.knowme.types.ScoringCriteria

import com.knowme.types.Index.IndexedDocument

/**
  * Created by admin on 16/2/19.
  */

class RankByBM25 extends ScoringCriteria{
  def Score(doc: IndexedDocument,fields: Any): Array[Double]  = {
    Array[Double](doc.bm25)
  }
}

