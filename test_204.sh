#!/bin/bash

# Clean up
make clean
make

# start server
echo "[starting agregation server...]"
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2


# Start content server 
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather3.txt" "ContentServer1_backup.txt" 1 > output.txt &
sleep 3

# Compare
file1="./test_204.txt"
file2="./output.txt"

# Compare the two files using the `cmp` command
if cmp -s "$file1" "$file2"; then
    echo -e "\e[32mPassed test: code 204 for empty content\e[0m"
else
    echo -e "\e[31mFailed test: code 204 for empty content \e[0m"
fi



# End testing
killall java
