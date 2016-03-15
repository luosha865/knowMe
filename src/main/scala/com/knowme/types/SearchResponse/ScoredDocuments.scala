package com.knowme.types.SearchResponse

/**
  * Created by admin on 16/2/22.
  */
import scala.collection.mutable.ArrayBuffer
import scala.util.Sorting

case class ScoredDocuments(docs: Array[ScoredDocument]){

  override def toString: String = {
    var output = ""
    for(doc<-docs){
      output += "[%d [".format(doc.docId)
      for(score <- doc.scores){
        output += "%d ".format((score.toDouble*1000).toInt)
      }
      output += "]] "
    }
    output
  }

  def getDocs(): Array[ScoredDocument] = {
    return docs
  }

  def slice(start: Int,end :Int) = {
    ScoredDocuments(docs.slice(start,end))
  }


  def ++(that : ScoredDocuments) : ScoredDocuments = {
    ScoredDocuments(this.docs ++ that.docs)
  }

  def sort(reverse: Boolean) = {
    if (reverse) {
      scala.util.Sorting.stableSort(docs, (x: ScoredDocument, y: ScoredDocument) => x < y)
    }else{
      scala.util.Sorting.stableSort(docs, (x: ScoredDocument, y: ScoredDocument) => x > y)
    }
  }

  def apply(i: Int): ScoredDocument ={
    docs.apply(i)
  }

  def length(): Int = {
    docs.length
  }

  def swap(i: Int, j: Int):Unit = {
    val v = docs(i)
    docs(i) = docs(j)
    docs(j) = v
  }


}

object ScoredDocuments{

  def apply(): ScoredDocuments = {
    new ScoredDocuments(Array[ScoredDocument]())
  }

}