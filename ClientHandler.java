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
    private String username;
    
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
        String request;
        try {
            username = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        

        // Keep listening fpr client requests
        while (client_soc.isConnected()) {
            try {
                // Get request
                request = bufferedReader.readLine();
                System.out.println(username + ": " + request);
                
                // reply
                bufferedWriter.write("you said" + request);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e){
                CloseConnection();
                break;
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