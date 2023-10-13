#!/bin/bash

# Clean up
make clean
make

# start server with lamport clock 1000
echo  -e "1000\n[]" > AggregationServer_backup.txt 
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2


# Start content server 1 and check if it will sync time with the Aggregation server
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" "ContentServer1_backup.txt" 1 > ./output.txt
sleep 3

# Compare
file1="./test_lamport.txt"
file2="./output.txt"

# Compare the two files using the `cmp` command
if cmp -s "$file1" "$file2"; then
    echo -e "\e[32mPassed test: Entities can sync Lamport time with each other\e[0m"
else
    echo -e "\e[31mFailed test: Entities can sync Lamport time with each other \e[0m"
fi




# End testing
killall java
