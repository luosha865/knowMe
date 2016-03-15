package com.knowme.types.SearchRequest

import com.knowme.types.ScoringCriteria.{RankByBM25, ScoringCriteria}

/**
  * Created by admin on 16/2/17.
  */


case class RankOptions(scoringCriteria: ScoringCriteria = new RankByBM25(), reverseOrder: Boolean = false,
                       outputOffset: Int = 0, maxOutputs: Int = 0)
