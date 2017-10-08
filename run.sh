#!/bin/sh

echo "-----------------------------------------------------"
echo "---------------- Unzipping log file! ----------------"
echo "-----------------------------------------------------"
gunzip data/2015_07_22_mktplace_shop_web_log_sample.log.gz

echo "-----------------------------------------------------"
echo "---------------- Building the fat jar! --------------"
echo "-----------------------------------------------------"
sbt clean assembly

echo "-----------------------------------------------------"
echo "---------------- Running the spark job! -------------"
echo "-----------------------------------------------------"
$SPARK_HOME/bin/spark-submit target/scala-2.11/app-assembly-0.1-SNAPSHOT.jar 900000 data/2015_07_22_mktplace_shop_web_log_sample.log

echo "-----------------------------------------------------"
echo "---------------- Zipping back log file! -------------"
echo "-----------------------------------------------------"
gzip data/2015_07_22_mktplace_shop_web_log_sample.log