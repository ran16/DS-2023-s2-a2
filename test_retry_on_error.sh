#!/bin/bash

# Clean up
make clean
make


# Start content server *before* the Aggregation server
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" "ContentServer1_backup.txt"  > Output_ContentServer.txt &
sleep 5

# start server
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2

# Read data
curl -X GET "http://127.0.0.1:4567/weather" > ./output.txt

# Compare
file1="./test_retry_on_error.txt"
file2="./output.txt"

# Compare the two files using the `cmp` command
if cmp -s "$file1" "$file2"; then
    echo -e "\e[32mPassed test: Content server retry on error\e[0m"
else
    echo -e "\e[31mFailed test: Content server retry on error \e[0m"
fi


# End testing
killall java
