#!/bin/bash

# Clean up
make clean
make

# start server
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2


# Start content server 1
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "bad_weather.txt" "ContentServer4_backup.txt" > ./output.txt &
sleep 0.5

# End testing
killall java


# Compare
file1="./test_500.txt"
file2="./output.txt"

# Compare the first 7 lines of both files using the `head` command
if cmp -s <(head -n 7 "$file1") <(head -n 7 "$file2"); then
    echo -e "\e[32mPassed test: code 500 \e[0m"
else
    echo -e "\e[31mFailed test: code 500 \e[0m"
fi





