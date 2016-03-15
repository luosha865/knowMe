package com.knowme.types.EngineInitOptions

import com.knowme.types.IndexerInitOptions.{IndexType, IndexerInitOptions}
import com.knowme.types.SearchRequest.RankOptions
import com.knowme.types.ScoringCriteria.RankByBM25

/**
  * Created by admin on 16/2/17.
  */
case class EngineInitOptions(usingSegmenter: Boolean = true, segmenterDictionaries: String = "dictionary.txt",
                             stopTokenFile: String = "stop_tokens.txt" , numSegmenterThreads: Int = 2,
                             numShards: Int = 2, indexerBufferLength: Int =2,
                             numIndexerThreadsPerShard: Int = 2, rankerBufferLength: Int = 2,
                             numRankerThreadsPerShard: Int = 2, indexerInitOptions : IndexerInitOptions = IndexerInitOptions(IndexType.LocationsIndex),
                             defaultRankOptions: RankOptions = RankOptions(new RankByBM25()), usePersistentStorage: Boolean = false,
                             persistentStorageFolder: String = "", persistentStorageShards: Int = 0)


