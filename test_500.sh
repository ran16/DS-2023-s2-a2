#!/bin/bash

# Clean up
make clean
make

# start server
echo "[starting agregation server...]"
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2


# Start content server 1
echo "[Waiting for Content server to send data from bad_weather.txt...]"
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "bad_weather.txt" "ContentServer4_backup.txt" > Output_ContentServer4.txt &
sleep 2

cat Output_ContentServer4.txt




# End testing
killall java
