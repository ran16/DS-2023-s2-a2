GREEN := \033[0;32m
RED := \033[0;31m
END := \033[0m

all:
	javac *.java

start:
	java AggregationServer

client:
	java GETClient "http://127.0.0.1:4567" 

content:
	java ContentServer "http://127.0.0.1:4567" "./weather_station1.txt"

clean:
	rm *.class
