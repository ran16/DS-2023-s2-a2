#!/bin/bash

# Clean up
make clean
make

# sleep and reset... 
sleep 3

# start server
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2

# Start content server 1,2,3
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" "ContentServer1_backup.txt" 1 > Output_ContentServer1.txt &
sleep 1 
# Make sure that server 2 starts after server 1 so that the result is predictable
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather2.txt" "ContentServer2_backup.txt" 1 > Output_ContentServer2.txt &
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather3.txt" "ContentServer3_backup.txt" 1 > Output_ContentServer3.txt &
sleep 2

# Start get clients
java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" > output.txt 


# Compare
file1="./test_3ContentServers.txt"
file2="./output.txt"

# Compare the two files using the `cmp` command
if cmp -s "$file1" "$file2"; then
    echo -e "\e[32mPassed test: 3 Content Servers updating weather in parallel, and updating depends on lamport clock when sending\e[0m"
else
    echo -e "\e[31mFailed test: 3 Content Servers updating weather in parallel, and updating depends on lamport clock when sending \e[0m"
    diff ./test_3ContentServers.txt /output.txt
fi



# End testing
killall java
