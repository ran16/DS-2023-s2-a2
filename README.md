# DS-2023-s2-a2

# General commands:
start server: <make start>
start client: <make client1> or <make client2> or <make client3>
run all tests: <make tests>


# Lamport Clock:
1. These events will triger an lamport clock update:
    * Recieving a message
    * Sending a message
2. When recieving a message, the reciever will compare its local clock with the recieved clock. If the recived clock is greater, then let local clock = recieved clock, otherwise no change. Then increase local clock by 1.
3. When sending a message, increase local clock by 1.
4. Content servers sync their lamport clock with the Aggregation server with GET time request, before it sends every weather update.


# Race condition:
    Race condition may happen when Aggreation Server's multiple threads try to update the database and lamport clock at the same time. To avoid this, I used "synchronized" methods to make sure that only one thread can execute the operation at any given time.

# Removing old entries:
When a content server stop sending messages for over 30 seconds, a time will go off and call the RemoveOldEntries() function. The function takes the disconnected content server's session ID, and look for entries with the same session ID in the database, and remove them.

# Fault Tolerance:
Both Content servers and Aggregation Servers are fault tolerant. They write weather data to a backup file on each data update, and they load the backup file when starting or restarting. The backup file include all the weather data and old lampot clock.
Both Content server and Get Client will retry sending requests if they couldn't connect to server or recienve error code 500.

# Tests:
(To run all the tests below, run <make tests>)
1. Testing GetClient requests for a valid station ID
        *run <./test_GET_stationID.sh>
2. Testing GetClient requests for ALL weather stations
        *run <./test_GET_all_stations.sh>
3. Testing special case: GetClient requests for an invalid station ID.
        *run <./test_GET_invalid_stationID.sh>
4. Testing text sending works; client, Aggregation server and content server processes start up and communicate; PUT operation works for one content server 
        *run <./test_PUT_and_GET.sh>
5. Testing 4 GET Clients
        *run <./test_4GETClients.sh>
6. Testing Aggregation server expunging expired data works (30s)
        * run <./test_expire.sh>
7. Testing Content Server retry on errors
        * run <./test_retry_on_error.sh>
8. Testing Lamport clocks
        * run <./test_lamport.sh>
9. Testing 3 Content Servers, and updating depends on the lamport clock when sending
        *run <./test_3ContentServers.sh>
10. Testing all error codes are implemented
        * run <./test_200_201.sh>
        * run <./test_204.sh>
        * run <./test_400.sh>
        * run <./test_500.sh>       
11. Testing aggregation servers are replicated and fault tolerant: after restarting, the weather data still persists.
        *run <./test_AG_FT.sh>
12. Testing content servers are replicated and fault tolerant: resend PUT request after crashing and restarting
        *run <./test_CS_FT.sh>



