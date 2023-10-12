GREEN := \033[0;32m
RED := \033[0;31m
END := \033[0m

all:
	javac -cp .:./gson-2.10.1.jar *.java



start:
	java -cp .:./gson-2.10.1.jar AggregationServer

client1:
	java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567"  IDS01

client2:
	java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" 

client3:
	java -cp .:./gson-2.10.1.jar GETClient "http://127.0.0.1:4567" IDS100

content1:
	java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather1.txt" "ContentServer1_backup.txt"

content2:
	java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather2.txt" "ContentServer2_backup.txt"

content3:
	java -cp .:./gson-2.10.1.jar ContentServer "http://127.0.0.1:4567" "./weather3.txt" "ContentServer3_backup.txt"

clean:
	rm *.class
	rm Output_*.txt
	killall java
