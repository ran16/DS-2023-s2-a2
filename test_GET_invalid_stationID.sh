#!/bin/bash

# expect9.txt

# Clean up
make clean
make

# start server
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2

# Send some data
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" "ContentServer1_backup.txt" > Output_ContentServer.txt &
sleep 2


# GetClient requests for an invalid station ID
curl -X GET "http://127.0.0.1:4567/weather/quack" > output.txt

# Compare
file1="./test_GET_invalid_stationID.txt"
file2="output.txt"

# Compare the two files using the `cmp` command
if cmp -s "$file1" "$file2"; then
    echo -e "\e[32mPassed test: GET invalid stationID \e[0m"
else
    echo -e "\e[31mFailed test: GET invalid stationID \e[0m"
fi

# End testing
killall java
