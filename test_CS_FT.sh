#!/bin/bash

# Clean up
make clean
make

# start server
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2


# Send some data
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "weather1.txt" "ContentServer1_backup.txt" 1 > Output_ContentServer1.txt &
pid_cs_server=$!
sleep 2

# Kill CS server
kill -kill $pid_cs_server


# Restart CS server
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "weather1.txt" "ContentServer1_backup.txt" 1 > ./output.txt &
sleep 2

# Compare
file1="./test_CS_FT.txt"
file2="./output.txt"

# Compare the two files using the `cmp` command
if cmp -s "$file1" "$file2"; then
    echo -e "\e[32mPassed test: Content Server resend PUT request after crashing and restarting\e[0m"
else
    echo -e "\e[31mFailed test: Content Server resend PUT request after crashing and restarting \e[0m"
fi




# End testing
killall java
