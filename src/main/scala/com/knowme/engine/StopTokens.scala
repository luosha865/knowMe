package com.knowme.engine

/**
  * Created by admin on 16/3/9.
  */

import scala.collection.mutable._

case class StopTokens(stopTokenFile: String) {

  val stopTokens = Set[String]()

  def isStopToken(token: String) : Boolean ={
    return stopTokens.contains(token)
  }

}
