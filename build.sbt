val sparkCore = "org.apache.spark" 	%% "spark-core" 	% "2.2.0"
val sparkSQL  = "org.apache.spark" 	%% "spark-sql" 		% "2.2.0"

lazy val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "com.mhsg",
  scalaVersion := "2.11.11"
)

lazy val app = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= Seq(sparkCore, sparkSQL),
    mainClass in run := Option("com.mhsg.paytm.Main")
  )
