package com.knowme.types.DocumentIndexData

/**
  * Created by admin on 16/3/10.
  */


case class DocumentIndexData(content: String, tokens: Array[TokenData]= Array[TokenData](), labels: Array[String] = Array[String](), fields: Any)