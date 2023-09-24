GREEN := \033[0;32m
RED := \033[0;31m
END := \033[0m

all:
	javac *.java

start:
	java AggregationServer

client:
	java GETClient "http://127.0.0.1:4567" 

clean:
	rm *.class
