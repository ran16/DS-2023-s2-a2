import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AggregationServer {
    private ServerSocket serverSocket;
    public static HashMap<String, WeatherEntry> database = new HashMap<>(); // used to store weather data. key is the station ID and the value is the corresponding weatherentry object
    public static int LamportClock = 0;
    public static int sessionID = 0; // Used to keep track of content servers.
    private static Parser parser = new Parser();;

    // constructor
    public AggregationServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    // This function increase the lamport clock by 1
    // Events that trigger clock are: recieving messages, sending messages.
    public synchronized static void UpdateClock(int recieved_time) {
        LamportClock  = (recieved_time > LamportClock ) ? recieved_time : LamportClock ;
        LamportClock++;
    }

    // Isssue session ID to client, and increase sessionID by 1.
    public synchronized static int GetSessionID(){
        int sid = sessionID;
        
        // increase session ID by 1
        sessionID++;

        // Issue the session ID to client
        return sid;
    }

    // This function lets the thread update the weather. "synchronized" keyword is used to protect race condition.
    // It returns the size of updated items on success, and -1 if fail. 
    public synchronized static int UpdateWeather(int sessionID, String new_data) {
        try {
            WeatherEntry[] entries = parser.JSON2Obj(new_data);
            // update the data
            for (WeatherEntry entry:entries) {
                // Add the session ID to the entry as sourceID.
                entry.addSourceID(sessionID);
                // update in database
                AggregationServer.database.put(entry.getStationID(), entry);
            }
            
            return entries.length; // success
        } catch (Exception e) {
            return -1; // failed due to invalid json format
        }        
    }

    // This function saves the database to the backup file in json format.
    public synchronized static void UpdateBackupFile(String FilePath) {
        // update the backup file
        parser.dump2File(database, LamportClock, FilePath);
    }


    // Remove all old entries that is logged by contentServerID if it becomes disconnected.
    public synchronized static void RemoveOldEntries(int contentServerID){
        // Create an iterator 
        Iterator<Map.Entry<String, WeatherEntry>> iterator = database.entrySet().iterator();

        // Iterate through the entries
        while (iterator.hasNext()) {
            Map.Entry<String, WeatherEntry> entry = iterator.next();
            if (entry.getValue().getSourceID() == contentServerID) {
                // Remove entry logged by this server
                iterator.remove();
            }
        }
    }

    public static void main(String[] args) throws IOException{
        // Get port number from command line argument
        int port_number = 4567;
        if (args.length > 0) {
            try {
                port_number = Integer.parseInt(args[0]);
            } catch (Exception e){
                port_number = 4567; // the argument is an invalid port number -- default to 4567
            }
        }

        // Create a server socket
        ServerSocket serverSocket = null;
        // Use try-catch to prevent failing to start because the port is not available.
        try {
            serverSocket = new ServerSocket(port_number);
        } catch(Exception e) {
            serverSocket = new ServerSocket(4567);
        }

        // Starts the server
        AggregationServer server = new AggregationServer(serverSocket);
        server.StartServer();
    }
    
    // This function starts the server.
    public void StartServer() {
        System.out.println("Starting aggregation server ...");
        
        try {
            System.out.println("Loading database from backup file ...");
            String[] backup = parser.readBackupFile("AggregationServer_backup.txt");
            // Load lamport clock from backup file
            LamportClock = Integer.valueOf(backup[0]);
            
            // Load data from backup file.
            WeatherEntry[] weatherEtries = parser.JSON2Obj(backup[1]);
            for (WeatherEntry entry : weatherEtries) {
                database.put(entry.getStationID(), entry);
            }

            // Connect to clients
            try {
                System.out.println("The aggregation server has started.");
                
                // Keep listening for connection requests
                while (!serverSocket.isClosed()) {
                    // Blocks till a connection request comes through
                    Socket client_soc = serverSocket.accept();        

                    // Create a client handler to handler the client request
                    ClientHandler clientHandler = new ClientHandler(client_soc);

                    // Spawn a new thread to handler the client request
                    Thread thread = new Thread(clientHandler);
                    thread.start();
                }
            } catch (IOException e) {
                CloseServerSocket();
            }
        } catch (Exception e) {
            System.out.println("Failed to load data from backup file.");
        }
    }

    // This function closes the server socket if it is not null.
    public void CloseServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}