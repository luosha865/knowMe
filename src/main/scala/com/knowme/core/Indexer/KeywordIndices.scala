package com.knowme.core.Indexer

import scala.collection.mutable.ArrayBuffer

/**
  * Created by admin on 16/2/19.
  */

case class KeywordIndices(docIds : ArrayBuffer[Int], frequencies: ArrayBuffer[Double]= ArrayBuffer[Double](), locations: ArrayBuffer[Array[Int]] = ArrayBuffer[Array[Int]]())
//{
//  // 下面的切片是否为空，取决于初始化时IndexType的值
//  var docIds : Array[Int] = null //Array[Int]() // 全部类型都有
//  var frequencies: Array[Double] = null//Array[Double]() // IndexType == FrequenciesIndex
//  var locations: Array[Array[Int]] = null// Array[Array[Int]]() // IndexType == LocationsIndex
//}