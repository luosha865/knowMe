package com.knowme.types.IndexerInitOptions

/**
  * Created by admin on 16/2/23.
  */


object IndexType extends Enumeration{
  type IndexType = Value
  val DocIdsIndex, FrequenciesIndex, LocationsIndex = Value
}