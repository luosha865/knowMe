package com.knowme.analysis

import com.huaban.analysis.jieba.JiebaSegmenter
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode
import collection.JavaConversions._

/**
  * Created by admin on 16/3/11.
  */

class JiebaTokenizer extends Tokenizer {

  val segmenter = new JiebaSegmenter()

  import Tokenizer.{Segment,Token}
  def analysis(text: String): Array[Segment] ={
    segmenter.process(text, SegMode.INDEX).map({
      x => Segment(x.startOffset,x.endOffset,Token(x.word))
    }).toArray
  }
}
