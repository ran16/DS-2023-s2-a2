#!/bin/bash

# Clean up
make clean
make

# start server
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2

# Start content server
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" "ContentServer1_backup.txt" > Output_ContentServer.txt &
pid_content_server=$!
sleep 2

# Get weather
java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" > ./output.txt

# Terminate Content Server
kill $pid_content_server
sleep 30

# Get weather again -- should be empty
echo "Content server terminated for over 30s..." >> ./output.txt
java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" >> output.txt

# Compare
file1="./test_expire.txt"
file2="./output.txt"

# Compare the two files using the `cmp` command
if cmp -s "$file1" "$file2"; then
    echo -e "\e[32mPassed test: Aggregation server expunging expired data works (30s)\e[0m"
else
    echo -e "\e[31mFailed test: Aggregation server expunging expired data works (30s) \e[0m"
fi

# End testing
killall java
