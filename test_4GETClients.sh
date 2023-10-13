#!/bin/bash

# Clean up
make clean
make

# start server
echo "starting agregation server..."
java -cp .:./gson-2.10.1.jar AggregationServer > Output_AggregationServer.txt &
sleep 2

# Start content server
echo "Content server sending data from weather1.txt ..."
java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" "ContentServer1_backup.txt" > Output_ContentServer.txt &
sleep 2

# Start get clients
java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" IDS01 > output1.txt 
java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" > output2.txt 
java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" IDS03 > output3.txt 
java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" > output4.txt 


# Compare
file1_1="./test_4GETClients1.txt"
file1_2="./test_4GETClients2.txt"
file1_3="./test_4GETClients3.txt"
file1_4="./test_4GETClients4.txt"

file2_1="output1.txt"
file2_2="output2.txt"
file2_3="output3.txt"
file2_4="output4.txt"

# Compare the two files using the `cmp` command
if cmp -s "$file1_1" "$file2_1"; then
    if cmp -s "$file1_2" "$file2_2"; then
        if cmp -s "$file1_3" "$file2_3"; then
            if cmp -s "$file1_4" "$file2_4"; then
                echo -e "\e[32mPassed test: 4 GET clients\e[0m"
            else
                echo -e "\e[31mFailed test: 4 GET clients \e[0m"
            fi 
        else
            echo -e "\e[31mFailed test: 4 GET clients \e[0m"
        fi
    else
        echo -e "\e[31mFailed test: 4 GET clients \e[0m"
    fi
else
    echo -e "\e[31mFailed test: 4 GET clients \e[0m"
fi



# End testing
killall java
