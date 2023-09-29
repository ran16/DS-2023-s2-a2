make clean
make
java -cp .:./gson-2.10.1.jar AggregationServer &
pid_server=$!

java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" &

java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:7050" &
pid_client1=$!

java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:7050" &
pid_client2=$!

java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:7050" &
pid_client3=$!

read -p "Finish? [y/n]" input
killall java

