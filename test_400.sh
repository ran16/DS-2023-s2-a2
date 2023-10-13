#!/bin/bash

# Clean up
make clean
make

# start server
echo "[starting agregation server...]"
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2


# Send an HTTP POST request, should recieve 400
curl -X POST "http://127.0.0.1:4567"  --fail 2> ./output.txt

# Compare
file1="./test_400.txt"
file2="./output.txt"

# Compare the two files using the `cmp` command
if cmp -s "$file1" "$file2"; then
    echo -e "\e[32mPassed test: code 400 Bad Request \e[0m"
else
    echo -e "\e[31mFailed test: code 400 Bad Request  \e[0m"
fi



# End testing
killall java
