#!/bin/bash

# Clean up
make clean
make

# start server
echo "[starting agregation server...]"
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2


# Send some data
echo "[Waiting for Content server to send data from weather1.txt...]"
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "weather1.txt" "ContentServer1_backup.txt" > Output_ContentServer1.txt &
pid_cs_server=$!
sleep 2

# Kill CS server
echo "Terminating Content server ..."
kill -kill $pid_cs_server

# View back up file
echo "[Content server's backup file: ]"
cat ContentServer1_backup.txt
echo "########################"
echo ""


# Restart CS server
echo "[Restart Content server to send data from weather1.txt...]"
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "weather1.txt" "ContentServer1_backup.txt" > Output_ContentServer1.txt &
sleep 2

cat Output_ContentServer1.txt





# End testing
killall java
