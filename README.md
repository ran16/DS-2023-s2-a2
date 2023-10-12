# DS-2023-s2-a2

# General commands:
start server: <make start>
start client: <make client1> or <make client2> or <make client3>


# Lamport Clock:
1. These events will triger an lamport clock update:
    * Recieving a message
    * Sending a message
2. When recieving a message, the reciever will compare its local clock with the recieved clock. If the recived clock is greater, then let local clock = recieved clock, otherwise no change. Then increase local clock by 1.
3. When sending a message, increase local clock by 1.


# Race condition:
    Race condition may happen when Aggreation Server's multiple threads try to update the database and lamport clock at the same time. To avoid this, I used "synchronized" methods to make sure that only one thread can execute the operation at any given time.

# Removing old entries:
When a content server stop sending messages for over 30 seconds, a time will go off and call the RemoveOldEntries() function. The function takes the disconnected content server's session ID, and look for entries with the same session ID in the database, and remove them.

# Fault Tolerance:
Both Content servers and Aggregation Servers are fault tolerant. They write weather data to a backup file on each data update, and they load the backup file when starting or restarting.

# Tests:
1. Testing text sending works; client, Aggregation server and content server processes start up and communicate; PUT operation works for one content server; GET operation works for 4 clients
    a) How to test:
        * run <test_PUT_and_GET.sh>
    b) Expected result: weather data sent by the content server viewable on the terminal. Data should match weather1.txt
2. Testing Aggregation server expunging expired data works (30s)
    a) How to test:
        * run <test_expire.sh>
    b) Expected result: before 30s expire, GetClient can view weather data sent by Content server (weather1.txt). After 30s expires, GetClient cannot view weather data sent by Content server (weather1.txt).
3. Testing retry on errors
    a) How to test:
        * run <test_retry_on_error.sh>
    b) Expected result: After aggregation gets killed, ContentServer keeps printing "failed to connect to server. Please check the host and port", and resend messages after aggregation server restarts.
4. Testing Lamport clocks
    a) Host to test:
        * run <test_lamport.sh>
    b) Expected result: Aggregation server prints updating weather message only if local time is less than the content server time.
5. Testing all error codes are implemented
    a) How to test:
        * run <test_200_201.sh>
        * run <test_204.sh>
        * run <test_400.sh>
        * run <test_500.sh>
6. Testing aggregation servers are replicated and fault tolerant
    a) How to test:
        *run <test_AG_FT.sh>
    b) Expected result: After killing the AG server and restarting, the weather data still persists.
7. Testing aggregation servers are replicated and fault tolerant
    a) How to test:
        *run <test_AG_FT.sh>
    b) Expected result: After killing the AG server and restarting, the weather data still persists.
8. Testing content servers are replicated and fault tolerant
    a) How to test:
        *run <test_CS_FT.sh>
    b) Expected result: After killing the CS server and restarting, the CS server will read the last PUT request from the backup server, and resend that request: "Resending PUT request...response code = 200"

