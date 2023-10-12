#!/bin/bash

# Clean up
make clean
make

# start server
echo "[starting agregation server...]"
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2


# Send an HTTP POST request, should recieve 400
curl -X POST "http://127.0.0.1:4567"







# End testing
killall java
