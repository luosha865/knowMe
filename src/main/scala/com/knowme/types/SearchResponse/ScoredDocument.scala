package com.knowme.types.SearchResponse

/**
  * Created by admin on 16/2/22.
  */
case class ScoredDocument(docId: Int,scores: Array[Double], tokenSnippetLocations: Array[Int], tokenLocations: Array[Array[Int]]){

  def <(doc2: ScoredDocument): Boolean = {
    val len = this.scores.length.min(doc2.scores.length)
    for(pos <- 0 until len){
      if (this.scores(pos) < doc2.scores(pos)) {
        return true
      }else if(this.scores(pos) > doc2.scores(pos)) {
        return false
      }
    }
    this.scores.length < doc2.scores.length
  }

  def ==(doc2: ScoredDocument): Boolean = {
    !(this < doc2) && !(doc2 < this)
  }

  def >(doc2: ScoredDocument): Boolean = {
    doc2 < this && !(this < doc2)
  }

}