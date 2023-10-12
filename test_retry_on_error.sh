#!/bin/bash

# Clean up
make clean
make

# start server
echo "starting agregation server..."
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
pid_agg_server=$!
sleep 2


# Start content server
echo "Content server sending data from weather1.txt ..."
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" "ContentServer1_backup.txt" > Output_ContentServer.txt &
sleep 3
cat Output_ContentServer.txt



# Terminate Aggregation Server
echo "Terminating Aggregation server..."
kill -kill $pid_agg_server
sleep 12


# Restart server
echo "Restarting agregation server..."
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 3

echo "Now Content server has reconnected with aggregation server"
cat Output_ContentServer.txt


# End testing
killall java
