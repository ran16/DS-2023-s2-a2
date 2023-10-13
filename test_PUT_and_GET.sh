#!/bin/bash

# Clean up
make clean
make

# start server
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2

# send PUT request
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" "ContentServer1_backup.txt" > Output_ContentServer.txt &
sleep 2

# Read weather
java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" > output.txt 


# Compare
file1="./test_PUT_and_GET.txt"
file2="./output.txt"

# Compare the two files using the `cmp` command
if cmp -s "$file1" "$file2"; then
    echo -e "\e[32mPassed test: GET and PUT request \e[0m"
else
    echo -e "\e[31mFailed test: GET and PUT request \e[0m"
fi


# End testing
killall java
