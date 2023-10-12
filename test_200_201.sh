#!/bin/bash

# Clean up
make clean
make

# start server
echo "[starting agregation server...]"
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2


# Start content server 1
echo "[Waiting for Content server to send data from weather1.txt ...]"
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" "ContentServer1_backup.txt" > Output_ContentServer1.txt &
sleep 20

# Start content server 2
echo "[Resent PUT request from backup files recive code 200.]"
echo "[For the first entry, Content server recieved code 201.]" 
echo "[The following entries Content server recieved code 200.]"
cat Output_ContentServer1.txt







# End testing
killall java
