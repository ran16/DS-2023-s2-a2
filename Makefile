GREEN := \033[0;32m
RED := \033[0;31m
END := \033[0m

all:
	javac *.java

start:
	java AggregationServer

client:
	java CalculatorClient

clean:
	rm *.class
