make clean
make
java AggregationServer &
pid_server=$!

java GETClient "http://127.0.0.1:7050" &
pid_client1=$!

java GETClient "http://127.0.0.1:7050" &
pid_client2=$!

java GETClient "http://127.0.0.1:7050" &
pid_client3=$!

read -p "Finish? [y/n]" input
kill -kill $pid_server

