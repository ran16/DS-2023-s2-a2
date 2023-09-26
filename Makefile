GREEN := \033[0;32m
RED := \033[0;31m
END := \033[0m

all:
	javac -cp .:./gson-2.10.1.jar *.java

test:
	java -cp .:./gson-2.10.1.jar TxtToJsonConverter

start:
	java AggregationServer

client:
	java GETClient "http://127.0.0.1:4567"  IDS60903

content:
	java ContentServer "http://127.0.0.1:4567" "./weather_station1.txt"

clean:
	rm *.class
