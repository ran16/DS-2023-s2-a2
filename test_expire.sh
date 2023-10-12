#!/bin/bash

# Clean up
make clean
make

# start server
echo "starting agregation server..."
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2

# Start content server
echo "Content server sending data from weather1.txt ..."
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" "ContentServer1_backup.txt" > Output_ContentServer.txt &
pid_content_server=$!
echo $pid_content_server
sleep 2

# Get weather
java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" > Output_GetClient.txt
echo ""
echo "Client can see weather data: "
cat "./Output_GetClient.txt"

# Terminate Content Server
echo "Terminating content server..."
kill $pid_content_server

echo "wait for 30 seconds"
sleep 30

# Get weather again -- should be empty
java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" > Output_GetClient.txt

# Expected result: Get client cannot see weather data displayed in terminal
echo ""
echo "Now client can't see weather data: "
cat "./Output_GetClient.txt"


# End testing
killall java
