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
    private Parser Parser = new Parser(); // create a Parser for string <-> JSON
    
    // Constructor
    public ClientHandler(Socket socket) {
        try {
            // This is the client socket
            this.client_soc = socket;

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
            // Read in the request
            String request = "";
            String line = bufferedReader.readLine();
            
            while (line != null && !line.isEmpty()) {
                request = request + line + "\n";
                line = bufferedReader.readLine();
            }
            System.out.println("quack: --------------\n"+request+"------------");
            
            // Parse the request
            ParseHTTPRequest(request);
        } catch (IOException e){
            CloseConnection();
        }
    }

    // This function parses the request and send response accordingly. It returns true if the request is valid, otherwise false.
    public void ParseHTTPRequest(String request) {
        try {
            // split the string by space
            String[] parts = request.split(" ");

            // Parse request: GET /weather HTTP/1.1\n Host:
            if (parts.length>1 && parts[0].equals("GET")) {
                String[] path_parts = parts[1].split("/");
                if (path_parts.length > 1 && path_parts[1].equals("weather")) {
                    if (path_parts.length > 2) {
                        SendMessage("requested for weather from station " + path_parts[2]);
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
            } else if (parts.length>1 && parts[0].equals("POST")) {
                if (parts[1].equals("/update-weather")) {
                    
                } else {
                    // method doest exist
                    SendMessage("400 error: Bad Request");
                }
            } else {
                // method doest exist
                System.out.println(request);
                SendMessage("400 error: Bad Request");
            }
        } catch (Exception e) {
            // Bad request
            e.printStackTrace();
            SendMessage("here 400 error: Bad Request");
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