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
sleep 2

# Start get clients
java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" IDS01 > Output_GetClient1.txt 
java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" > Output_GetClient2.txt 
java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" IDS03 > Output_GetClient3.txt 
java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" > Output_GetClient4.txt 


# Expected result: Get client can view the weather data displayed in terminal
echo ""
echo "Client 1 requested for station IDS01 data: "
cat "./Output_GetClient1.txt"
echo ""
echo "Client 2 requested for all data: "
cat ./Output_GetClient2.txt
echo ""
echo "Client 3 requested for station IDS03 data: "
cat ./Output_GetClient3.txt
echo ""
echo "Client 4 requested for all data: "
cat ./Output_GetClient4.txt


# End testing
killall java
