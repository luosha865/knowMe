package com.knowme.core.Ranker

import com.knowme.types.Index.IndexedDocument
import com.knowme.types.ScoringCriteria.ScoringCriteria

/**
  * Created by admin on 16/2/24.
  */

case class DummyScoringCriteria(threshold: Double = 0.0) extends ScoringCriteria{

  def Score(doc: IndexedDocument, fields: Any): Array[Double] ={
    if(fields.isInstanceOf[DummyScoringFields]){
      val dsf: DummyScoringFields = fields.asInstanceOf[DummyScoringFields]
      val value = dsf.counter.toDouble + dsf.amount
      if(value < threshold){
        return Array[Double]()
      }
      return Array[Double](value)
    }
    Array[Double]()
  }
}
