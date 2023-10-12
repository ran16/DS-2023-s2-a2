#!/bin/bash

# Clean up
make clean
make

# start server
echo "[starting agregation server...]"
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
pid_agg_server=$!
sleep 2


# Send some data
echo "[Waiting for Content server to send data from weather1.txt...]"
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "weather1.txt" "ContentServer1_backup.txt" > Output_ContentServer1.txt &
sleep 2

# Kill AG server
echo "[Terminating Aggregation server...]"
kill -kill $pid_agg_server
sleep 4

# Restart AG server
echo "[Restarting agregation server...]"
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 3

# Get data, and the data is still there
curl -X GET "http://127.0.0.1:4567/weather"




# End testing
killall java
