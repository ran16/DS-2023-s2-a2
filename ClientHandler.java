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
        // Keep listening fpr client requests
        while (client_soc.isConnected()) {
            String request;
            try {
                // Read in the request
                request = bufferedReader.readLine();
                
                // Parse the request
                ParseHTTPRequest(request);
            } catch (IOException e){
                CloseConnection();
                break;
            }
        }
    }

    // This function parses the request and send response accordingly. It returns true if the request is valid, otherwise false.
    public boolean ParseHTTPRequest(String request) {
        // split the string by space
        String[] parts = request.split(" ");

        // GET /weather HTTP/1.1\n Host:
        if (parts[0].equals("GET")) {
            if (parts[1].equals("/weather")) {
                // SendWeather();
                SendMessage("Sending weather.... not yet implemented. But it looks pretty sunny today!");
            } else {
                // method doest exist
                SendMessage("405 error: Method Not Allowed");
                return false;
            }
        } else if (parts[0].equals("POST")) {
            if (parts[1].equals("/update-weather")) {
                
            } else {
                // method doest exist
                SendMessage("400 error: Bad Request");
                return false;
            }
        } else {
            // method doest exist
            SendMessage("400 error: Bad Request");
            return false;
        }
        return true;
    }

    public void SendMessage(String msg) {
        try {
            this.bufferedWriter.write(msg);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        } catch (IOException e){
            CloseConnection();
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