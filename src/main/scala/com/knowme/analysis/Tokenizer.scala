package com.knowme.analysis

/**
  * Created by admin on 16/3/9.
  */
import com.huaban.analysis.jieba._
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode


abstract class Tokenizer {

  import Tokenizer.{Segment,Token}
  def analysis(text: String): Array[Segment]
}


object Tokenizer{

  case class Token(text: String)
  case class Segment(start: Int, end: Int, token: Token)
}
