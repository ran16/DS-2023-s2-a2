GREEN := \033[0;32m
RED := \033[0;31m
END := \033[0m

all:
	javac -cp .:./gson-2.10.1.jar *.java

test:
	java -cp .:./gson-2.10.1.jar TxtToJsonConverter

start:
	java -cp .:./gson-2.10.1.jar AggregationServer

client:
	java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567"  IDS60901

client2:
	java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" 

client3:
	java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" IDS40903

content1:
	java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt"

content2:
	java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather2.txt"

clean:
	rm *.class
