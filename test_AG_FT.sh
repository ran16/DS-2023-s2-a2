#!/bin/bash

# Clean up
make clean
make

# start server
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
pid_agg_server=$!
sleep 2


# Send some data
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "weather1.txt" "ContentServer1_backup.txt" > Output_ContentServer1.txt &
sleep 2

# Get data
curl -X GET "http://127.0.0.1:4567/weather" > ./output1.txt

# Kill AG server
kill -kill $pid_agg_server
sleep 4

# Restart AG server
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 3

# Get data, and the data is still there
curl -X GET "http://127.0.0.1:4567/weather" > ./output2.txt

# Compare
file1="./output1.txt"
file2="./output2.txt"

# Compare the two files using the `cmp` command
if cmp -s "$file1" "$file2"; then
    echo -e "\e[32mPassed test: Aggregation Server persists data after crash\e[0m"
else
    echo -e "\e[31mFailed test: Aggregation Server persists data after crash \e[0m"
fi



# End testing
killall java
