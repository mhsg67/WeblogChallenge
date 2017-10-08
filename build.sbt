val sparkCore = "org.apache.spark" 	%% "spark-core" 	% "2.2.0"
val sparkSQL  = "org.apache.spark" 	%% "spark-sql" 		% "2.2.0"

lazy val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "com.mhsg",
  scalaVersion := "2.11.11",
  logLevel := Level.Error
)

lazy val app = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= Seq(sparkCore, sparkSQL),
    mainClass in run := Some("com.mhsg.paytm.Main"),
    mainClass in assembly := Some("com.mhsg.paytm.Main"),
    logLevel in assembly := Level.Error,
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )
