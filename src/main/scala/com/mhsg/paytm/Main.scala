package com.mhsg.paytm

import java.text.SimpleDateFormat

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

import scala.util.Try

/**
  * Created by mohammadhosseinsedighigilani on 2017-10-08.
  */
object Main {

  val IP = "ip"
  val URL = "url"
  val INT = "int"
  val LAG = "lag"
  val NULL = "null"
  val REQUEST = "request"
  val IP_PORT = "ip_port"
  val DURATION = "duration"
  val TIMESTAMP = "timestamp"
  val DATE_TIME = "date_time"
  val USER_AGENT = "user_agent"
  val SESSION_ID = "sessionId"
  val IS_NEW_SESSION = "is_new_session"
  val UNIQUE_URL_COUNT = "UniqueURLCount"
  val COUNT_DISTINCT_URL = "count(DISTINCT url)"


  val IP_COL = col(IP)
  val LAG_COL = col(LAG)
  val COL_0 = col("_c0")
  val COL_2 = col("_c2")
  val COL_11 = col("_c11")
  val COL_12 = col("_c12")
  val IP_PORT_COL = col(IP_PORT)
  val REQUEST_COL = col(REQUEST)
  val DURATION_COL = col(DURATION)
  val TIMESTAMP_COL = col(TIMESTAMP)
  val DATE_TIME_COL = col(DATE_TIME)
  val USER_AGENT_COL = col(USER_AGENT)
  val SESSION_ID_COL = col(SESSION_ID)


  val rawFileReadOption = Map(("header" -> "false"), ("mode" -> "DROPMALFORMED"), ("delimiter" -> " "),
    ("quote" -> "\""), ("nullValue" -> "null"), ("treatEmptyValuesAsNulls" -> "true"))

  val timeConversionUDF = udf((t: String) => {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
    Try(format.parse(t)).map(x => x.getTime).getOrElse(-1L)
  })

  def main(args: Array[String]): Unit = {
    if (args.length == 2) {
      val sparkSession = SparkSession.builder().appName("mhsg_paytm_challenge").master("local[*]").getOrCreate()
      sparkSession.sparkContext.setLogLevel("ERROR")


      val gap = args(0).toLong
      val dataFilePath = args(1)
      val rawDataDF = sparkSession.read.format("csv").options(rawFileReadOption).load(dataFilePath)

      val requiredColumnsDF = rawDataDF.select(COL_0.as(DATE_TIME), COL_2.as(IP_PORT), COL_11.as(REQUEST), COL_12.as(USER_AGENT))
      val removedNullsDF = requiredColumnsDF.filter((USER_AGENT_COL =!= NULL) && (IP_PORT_COL =!= NULL) && (DATE_TIME_COL =!= NULL))
      val cleanedUpIPsDF = removedNullsDF.withColumn(IP, split(IP_PORT_COL, ":").getItem(0)).drop(IP_PORT_COL)
      val cleanedUpURLsDF = cleanedUpIPsDF.withColumn(URL, split(REQUEST_COL, " ").getItem(1)).drop(REQUEST_COL)
      val cleanedUpTimestampsDF = cleanedUpURLsDF.withColumn(TIMESTAMP, timeConversionUDF(DATE_TIME_COL)).drop(DATE_TIME_COL)
        .where(TIMESTAMP_COL =!= -1)

      val byIPAndUserAgentOrderByTimestamp = Window.partitionBy(IP_COL, USER_AGENT_COL).orderBy(TIMESTAMP_COL)
      val laggedDF = cleanedUpTimestampsDF.withColumn(LAG, lag(TIMESTAMP_COL, 1, 0) over byIPAndUserAgentOrderByTimestamp)
      val isNewSessionDF = laggedDF.withColumn(IS_NEW_SESSION, (TIMESTAMP_COL - LAG_COL > gap).cast(INT))
      val sessionizedDF = isNewSessionDF.withColumn(SESSION_ID, sum(IS_NEW_SESSION) over byIPAndUserAgentOrderByTimestamp)

      val sessionsDurationsDF = sessionizedDF.groupBy(IP_COL, USER_AGENT_COL, SESSION_ID_COL)
        .agg((max(TIMESTAMP_COL) - min(TIMESTAMP_COL)).as(DURATION)).where(DURATION_COL =!= 0)
      sessionsDurationsDF.agg(avg(DURATION_COL)).show(false)

      val mostEngagingRow = sessionsDurationsDF.reduce((a, b) => if (a.getLong(3) > b.getLong(3)) a else b)
      println(s"Most Engaging user!")
      println(s"IP: ${mostEngagingRow.getString(0)}, UserAgent: ${mostEngagingRow.getString(1)}")

      sessionizedDF.groupBy(IP_COL, USER_AGENT_COL, SESSION_ID_COL).agg(countDistinct(URL))
        .withColumnRenamed(COUNT_DISTINCT_URL, UNIQUE_URL_COUNT).write.parquet("data/URLCountPerSession")

      sparkSession.close()
    }
    else {
      throw new IllegalArgumentException()
    }
  }
}
