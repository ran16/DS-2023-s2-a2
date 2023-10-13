#!/bin/bash

# Clean up
make clean
make

# start server
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2

# Start content server
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" "ContentServer1_backup.txt" 2 > output.txt &
sleep 20

# Compare
file1="./test_200_201.txt"
file2="./output.txt"

# Compare the two files using the `cmp` command
if cmp -s "$file1" "$file2"; then
    echo -e "\e[32mPassed test: code 200 and 201 \e[0m"
else
    echo -e "\e[31mFailed test: code 200 and 201 \e[0m"
fi








# End testing
killall java
