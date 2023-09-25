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
        try {
            // Read in the first line of request
            String request = bufferedReader.readLine();

            // Check if the request is GET or PUT
            if (request.split(" ")[0].equals("GET")) {
                ParseGETRequest(request);
            } else if (request.split(" ")[0].equals("PUT")) {
                ParsePUTRequest(request);
            } else {
                SendMessage("400: Bad request\n");
            }
        } catch (IOException e){
            CloseConnection();
        }
    }

    // This function parses PUT request and send response accordingly
    public void ParsePUTRequest(String request) {
        try {
            // Read till the connection closes or the closing bracket
            String line = bufferedReader.readLine();
            while (line != null && !line.equals("}")) {
                request = request + line + "\n";
                line = bufferedReader.readLine();
            }
            request = request + line + "\n";
            System.out.println("quack: --------------\n"+request+"------------");

            // Parse request
            String[] resource = request.split(" ")[1].split("/");
            // If it is /weather/stationID or /weather
            if (resource.length > 1 && resource[1].equals("weather.json")) {
                SendMessage("HTTP/1.1 200 OK\r\n");
            } else {
                // method doest exist
                SendMessage("400 error: Bad request\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
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

            // Parse request
            String[] resource = request.split(" ")[1].split("/");
            // If it is /weather/stationID or /weather
            if (resource.length > 1 && resource[1].equals("weather")) {
                if (resource.length > 2) { // If it is /weather/stationID
                    SendMessage("requested for weather from station " + resource[2]);
                } else {
                    SendMessage("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: json\r\n" +
                    "\r\n" + this.Parser.txt2JSON("./weather.txt"));
                    CloseConnection();
                }
            } else {
                // method doest exist
                SendMessage("405 error: Method Not Allowed");
            }
        } catch (IOException e) {
            e.printStackTrace();
            CloseConnection();
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