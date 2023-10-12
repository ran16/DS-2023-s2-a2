#!/bin/bash

# Clean up
make clean
make

# start server
echo "[starting agregation server...]"
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2


# Start content server 1
echo "[Waiting for Content server to send data from weather3.txt, where weather3.txt is empty ...]"
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather3.txt" "ContentServer1_backup.txt" > Output_ContentServer3.txt &
sleep 3

# Start content server 2
echo "[For empty entry, Content server recieved code 204:]"
cat Output_ContentServer3.txt







# End testing
killall java
