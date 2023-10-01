import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket client_soc;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Parser Parser; // create a Parser for string <-> JSON

    // Constructor
    public ClientHandler(Socket socket) {
        try {
            // This is the client socket
            this.client_soc = socket;
            this.Parser = new Parser();

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

                // increase clock
                AggregationServer.tick();

                // Check if the request is GET or PUT
                if (request != null && request.split(" ")[0].equals("GET")) {
                    // validate the header
                    if (request.matches("GET /weather(/[a-zA-Z0-9]*)? .*")) {
                        ParseGETRequest(request+"\n");
                    } else {
                        SendMessage("HTTP/1.1 400 Bad Request\r\n","");
                        CloseConnection();
                    }
                } else if (request != null && request.split(" ")[0].equals("PUT")) {
                    // validate the header
                    if (request.matches("PUT /weather.json .*")) {
                        ParsePUTRequest(request+"\n");
                    } else {
                        SendMessage("HTTP/1.1 400 Bad Request\r\n","");
                        CloseConnection();
                    }
                } else {
                    SendMessage("HTTP/1.1 400 Bad Request\r\n","");
                }
            } catch (IOException e){
                CloseConnection();
                break;
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

            // Get the data in the body of the PUT request
            String new_data = Parser.extractBody(request);

            // Check for empty requests
            if (new_data.isEmpty() || new_data.equals("[]")) {
                SendMessage("HTTP/1.1 204 No Content\r\n", "");
            } else {
                SendMessage("HTTP/1.1 200 OK\r\n","");
                // Update the weather using the new data
                UpdateWeather(new_data);
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

            // Get the station ID from request
            String[] parts = request.split(" ")[1].split("/");
            String body = "";
            if (parts.length > 2) { // If it is /weather/stationID
                // find the station's entry
                WeatherEntry result = AggregationServer.database.get(parts[2]);
                if (result != null) {
                    body = Parser.Obj2JSON(result);
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
        AggregationServer.tick();
        header = header + "Lamport Time: " + AggregationServer.LamportClock+"\r\n";

        // if there is a body, add line break and square brackets.
        if (!body.isEmpty()) {
            body = "\r\n[\n" + body + "\n]";
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