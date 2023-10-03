import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler implements Runnable {
    private Socket client_soc;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Parser Parser; // create a Parser for string <-> JSON
    private Clock clock; // the Clock library
    private int sessionID; // to keep track of clients, especially content servers.
    private Timer timer; // Used to time content server acitivity

    // Constructor
    public ClientHandler(Socket socket) {
        try {
            // This is the client socket
            this.client_soc = socket;
            this.Parser = new Parser();
            this.clock = new Clock();
            this.sessionID = AggregationServer.sessionID;
            AggregationServer.sessionID++;

            // Create a timer to close the connection after 30 seconds of inactivity
            this.timer = new Timer();
            timer.schedule(new CloseConnectionTask(client_soc, sessionID), 30000);

            // Turn the socket's byte stream into char stream, and wrap it in a buffer for both read and write.
            this.bufferedReader = new BufferedReader(new InputStreamReader(client_soc.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(client_soc.getOutputStream()));
        } catch (IOException e){
            CloseConnection();
        }
    }

    // This is the code that will be executed in a thread, where the handling logic is implemented.
    @Override
    public void run() {
        // Listen for client requests
        while (this.client_soc.isConnected() && !this.client_soc.isClosed()) {
            try {
                // Read in the first line of request
                String request = bufferedReader.readLine();

                // Check if the request is GET or PUT
                if (request != null && request.matches("GET /weather(/[a-zA-Z0-9]*)? .*")) {
                    ParseGETRequest(request+"\n");
                } else if (request != null && request.matches("PUT /weather.json .*")) {
                    ParsePUTRequest(request+"\n");
                } else if (request != null && request.matches("GET /time .*")) {
                    System.out.println("sync time... reqeust");
                    ParseGETTimeRequest(request+"\n");
                } else {
                    SendMessage("HTTP/1.1 400 Bad Request\r\n","");
                }

                // Reset the timer upon receiving a message
                timer.cancel();
                timer.purge();
                timer = new Timer();
                timer.schedule(new CloseConnectionTask(client_soc, sessionID), 30000);
            } catch (IOException e){
                CloseConnection();
                break;
            }
        } 
    }

    // Define the action when timeout happens
    private static class CloseConnectionTask extends TimerTask {
        private final Socket clientSocket;
        private final int sessionID;

        CloseConnectionTask(Socket clientSocket, int sessionID) {
            this.clientSocket = clientSocket;
            this.sessionID = sessionID;
        }

        @Override
        public void run() {
            // Close the client connection due to inactivity
            try {
                System.out.println("Closing connection to Content Server " + sessionID + " due to inactivity.");
                if (clientSocket != null) {
                    // InputStream inputStream = clientSocket.getInputStream();
                    // OutputStream outputStream = clientSocket.getOutputStream();

                    // if (inputStream != null) {
                    //     inputStream.close();
                    // }
                    // if (outputStream != null) {
                    //     outputStream.close();
                    // } 
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // This function parses PUT request and send response accordingly
    public void ParsePUTRequest(String request) {
        try {
            // Read till the connection closes or the closing square bracket
            String line = bufferedReader.readLine();
            while (line != null && !line.equals("]")) {
                request = request + line + "\n";
                line = bufferedReader.readLine();
            }
            request = request + line + "\n";

            // Get timestamp from request
            int recieved_time = clock.GetRecievedTime(request);
            // Make a copy of the old time
            int old_time = AggregationServer.LamportClock;
            // Update clock
            AggregationServer.UpdateClock(recieved_time);

            // Get the data in the body of the PUT request
            String new_data = Parser.extractBody(request);

            // Check for empty requests
            if (new_data.isEmpty() || new_data.equals("[]")) {
                SendMessage("HTTP/1.1 204 No Content\r\n", "");
            } else {
                SendMessage("HTTP/1.1 200 OK\r\n","");
                // Update the weather using the new data
                if (old_time < recieved_time) {
                    System.out.println(old_time + " < " + recieved_time+ " ==> update weather!");
                    UpdateWeather(new_data);
                } else {
                    System.out.println(old_time + " > " + recieved_time+ " ==> no no no updating");
                }
            }          
        } catch (IOException e) {
            CloseConnection();
        }    
    }

    // This function parses GET request and send response accordingly
    public void ParseGETRequest(String request) {
        try {
            // Read till the connection closes or the closing bracket
            String line = bufferedReader.readLine();
            while (line != null && !line.isEmpty()) {
                request = request + line + "\n";
                line = bufferedReader.readLine();
            }
            request = request + line + "\n";
            System.out.println("quack: --------------\n"+request+"------------");

            // Get timestamp from request
            int recieved_time = clock.GetRecievedTime(request);
            AggregationServer.UpdateClock(recieved_time);

            // Get the station ID from request
            String[] parts = request.split(" ")[1].split("/");

            // return data
            String body = "";
            if (parts.length > 2) { // If it is /weather/stationID
                // find the station's entry
                WeatherEntry result = AggregationServer.database.get(parts[2]);
                if (result != null) {
                    body = "[\n" + Parser.Obj2JSON(result) + "\n]";
                }
            } else {
                // send all weather data
                body = this.Parser.dump(AggregationServer.database);
            }
            String header = "HTTP/1.1 200 OK\r\n" + "Content-Type: json\r\n";          
            SendMessage(header, body);
            CloseConnection();
        } catch (IOException e) {
            CloseConnection();
        }    
    }

    // This function parses GET time request and respond with server's lamport time.
    public void ParseGETTimeRequest(String request) {
        try {
            // Read till empty line
            String line = bufferedReader.readLine();
            while (line != null && !line.isEmpty()) {
                request = request + line + "\n";
                line = bufferedReader.readLine();
            }
            request = request + line + "\n";
            System.out.println("Incoming request: --------------\n"+request+"------------");

            // Get timestamp from request
            int recieved_time = clock.GetRecievedTime(request);
            AggregationServer.UpdateClock(recieved_time);

            // return data
            String body = "";
            String header = "HTTP/1.1 200 OK\r\n" + "Content-Type: txt\r\n";          
            SendMessage(header, body);
        } catch (IOException e) {
            CloseConnection();
        }    
    }

    // This function lets the thread update the weather. "synchronized" keyword is used to protect race condition.
    public synchronized void UpdateWeather(String new_data) {
        // Convert the data entries to WeaterEntry objects
        try {
            WeatherEntry[] entries = Parser.JSON2Obj(new_data);

            for (WeatherEntry entry:entries) {
                // update in database
                AggregationServer.database.put(entry.getID(), entry);
            } 
        } catch (Exception e) {
            // Incorrect JSON format
            SendMessage("HTTP/1.1 500 Internal Server Error\r\n","");
        }   
    }

    public void SendMessage(String header, String body) {
        // Everytime the Aggregation server sends a message, it increase its clock and send the time with the message.
        AggregationServer.LamportClock++;
        header = header + "Lamport Time: " + AggregationServer.LamportClock+"\r\n";

        // if there is a body, add line break .
        if (!body.isEmpty()) {
            body = "\r\n" + body;
        }
        
        if (client_soc.isConnected()) {
            try {
                this.bufferedWriter.write(header + body);
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            } catch (IOException e){
                CloseConnection();
            }
        }    
    }

    // This function closes the connection
    public void CloseConnection() {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (client_soc != null) {
                client_soc.close();
                System.out.println("Client is disconnected");
            }
        } catch (IOException e) {
            System.out.println("Failed to close connection");
        }
    }
}