GREEN := \033[0;32m
RED := \033[0;31m
END := \033[0m

all:
	javac *.java

start:
	java AggregationServer 

client1:
	java GETClient "http://127.0.0.1:7050" "Alice"

client2:
	java GETClient "http://127.0.0.1:7050" "Bob"

client3:
	java GETClient "http://127.0.0.1:7050" "Charlie"

clean:
	rm *.class
