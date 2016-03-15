package com.knowme.types.ScoringCriteria

import com.knowme.types.Index.IndexedDocument

/**
  * Created by admin on 16/2/17.
  */
// 评分规则通用接口
trait ScoringCriteria {
  // 给一个文档评分，文档排序时先用第一个分值比较，如果分值相同则转移到第二个分值，以此类推。
  // 返回空切片表明该文档应该从最终排序结果中剔除。
  def Score (doc: IndexedDocument,fields: Any): Array[Double]
}



