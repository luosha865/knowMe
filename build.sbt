name := "knowMe"

version := "1.0"

scalaVersion := "2.12.4"

scalacOptions += "-Ypartial-unification"


libraryDependencies += "org.deephacks.lmdbjni" % "lmdbjni" % "0.4.6"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.8"
libraryDependencies += "com.huaban" % "jieba-analysis" % "1.0.2"
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.5"
libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.5"



