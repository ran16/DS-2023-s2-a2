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
        while (this.client_soc.isConnected()) {
            try {
                // Read in the first line of request
                String request = bufferedReader.readLine();

                // Check if the request is GET or PUT
                if (request.split(" ")[0].equals("GET")) {
                    // validate the header
                    if (request.matches("GET /weather(/[a-zA-Z0-9]*)? .*")) {
                        ParseGETRequest(request+"\n");
                    } else {
                        SendMessage("Error 400 Bad request\n");
                        CloseConnection();
                    }
                } else if (request.split(" ")[0].equals("PUT")) {
                    // validate the header
                    if (request.matches("PUT /weather.json .*")) {
                        ParsePUTRequest(request+"\n");
                    } else {
                        SendMessage("Error 400 Bad request\n");
                        CloseConnection();
                    }
                } else {
                    SendMessage("Error 400 Bad request\n");
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
                SendMessage("204: no content\n");
            } else {
                SendMessage("HTTP/1.1 200 OK\r\n");
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
            String payload = "";
            if (parts.length > 2) { // If it is /weather/stationID
                // find the station's entry
                WeatherEntry result = AggregationServer.database.get(parts[2]);
                if (result != null) {
                    payload = Parser.Obj2JSON(result);
                }
                
                SendMessage("HTTP/1.1 200 OK\r\n" +
                "Content-Type: json\r\n" +
                "\r\n" + "[\n"+payload +"\n]\n");
                CloseConnection();
            } else {
                // send all weather data
                payload = this.Parser.dump(AggregationServer.database);
                SendMessage("HTTP/1.1 200 OK\r\n" +
                "Content-Type: json\r\n" +
                "\r\n" + payload + "\n");
                CloseConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            SendMessage("Error 500 internal server error");
        }   
    }

    public void SendMessage(String msg) {
        if (client_soc.isConnected()) {
            try {
                this.bufferedWriter.write(msg);
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            } catch (IOException e){
                CloseConnection();
            }
        }    
    }

    // This function closes the connection
    public void CloseConnection() {
        System.out.println("Client disconnected.");
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (client_soc != null) {
                client_soc.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}