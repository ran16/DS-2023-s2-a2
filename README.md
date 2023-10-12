# DS-2023-s2-a2

start server: <make start>
start client: <make client1> or <make client2> or <make client3>
test: 

Lamport Clock:
1. These events will triger an lamport clock update:
    * Recieving a message
    * Sending a message
2. When recieving a message, the reciever will compare its local clock with the recieved clock. If the recived clock is greater, then let local clock = recieved clock, otherwise no change. Then increase local clock by 1.
3. When sending a message, increase local clock by 1.


Race condition:
    Race condition may happen when Aggreation Server's multiple threads try to update the database and lamport clock at the same time. To avoid this, I used "synchronized" methods to make sure that only one thread can execute the operation at any given time.

Removing old entries:
When a content server stop sending messages for over 30 seconds, a time will go off and call the RemoveOldEntries() function. The function takes the disconnected content server's session ID, and look for entries with the same session ID in the database, and remove them.

Tests:
1. Testing text sending works; client, Aggregation server and content server processes start up and communicate; PUT operation works for one content server; GET operation works for 4 clients
    a) How to test:
        * run <./test_PUT_and_GET.sh>
    b) Expected result: weather data sent by the content server viewable on the terminal. Data should match weather1.txt
2. Testing Aggregation server expunging expired data works (30s)
    a) How to test:
        * run <./test_expire.sh>
    b) Expected result: