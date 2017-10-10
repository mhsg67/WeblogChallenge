# Weblog challenge
This is my solution for Paytm Labs' WeblogChallenge.

To run this project you only need to have sbt installed and SPARK_HOME be set. Then you can use run.sh command to
build and run my solution.

For now, I've assumed the session window time is 15 minutes.

# Some insights

1. The whole data set has 1158500 number of logs but 12 of them does not follow standard pattern (following) so I drop
 them when tried to load data set.

 ```
 timestamp elb client:port backend:port request_processing_time backend_processing_time response_processing_time elb_status_code backend_status_code received_bytes sent_bytes "request" "user_agent" ssl_cipher ssl_protocol
```

2. To find a unique user, I've used combination of IP address and UserAgent.
3. Following picture show the number of each intervals in data set. The interval has been calculated as the elapsed time between two consecutive request for a specific user.

<p align="center">
  <img src="/data/Intervals.png" >
</p>

4. Based on figure, there are two possible candidate for cut off time, 30-35 minutes or 120-125 minutes. Since more tha 99%
of intervals are less than 30 minutes and 120 minutes (or 2 hours) is relatively long time for being inactive, I've chosen 30
minutes as gap to separate two consecutive sessions.

# Answer to the questions

1. Consider the cut off time for considering a session has been finished is 30 minutes.

2. Average session duration is 706003 milliseconds.

3. The most engaged user has "-" as user-agent and 220.226.206.7 IP address

4. Count of unique URL visits per session will be saved in data folder after running "run" script.

