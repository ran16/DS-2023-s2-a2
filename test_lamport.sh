#!/bin/bash

# Clean up
make clean
make

# start server
echo "starting agregation server..."
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2


# Start content server 1
echo "Content server sending data from weather1.txt ..."
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" "ContentServer1_backup.txt" > Output_ContentServer1.txt &
sleep 3

# Start content server 2
echo "Content server sending data from weather2.txt ..."
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" "ContentServer1_backup.txt" > Output_ContentServer2.txt &
sleep 5

echo ""
echo "Aggregation Server updating weather by compairing Lamport clocks:"
cat ./Output_AggregationServer.txt






# End testing
killall java
