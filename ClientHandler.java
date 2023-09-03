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

    // This is the code that will be executed in a thread
    @Override
    public void run() {
        String request;

        // Keep listening fpr client requests
        while (client_soc.isConnected()) {
            try {
                // Get request
                request = bufferedReader.readLine();
                
                // reply
                bufferedWriter.write("you said" + request);
                bufferedWriter.newLine();
                bufferedWriter.flush();
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}