import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

public class GETClient {
    private Socket my_soc;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public GETClient(Socket socket) {
        try {
            this.my_soc = socket;
            // Turn the socket's byte stream into char stream, and wrap it in a buffer for both read and write.
            this.bufferedReader = new BufferedReader(new InputStreamReader(my_soc.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(my_soc.getOutputStream()));
        } catch (IOException e) {
            CloseConnection();
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
            if (my_soc != null) {
                my_soc.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws IOException{
        // Get the server name and port from commandline
        URL url = new URL(args[0]);
        String host = url.getHost();
        int port = url.getPort();

        // Connect
        Socket socket = new Socket(host, port);
        GETClient client = new GETClient(socket);
        System.out.println("Client is connected");

        // Get client username
        client.bufferedWriter.write(args[1]);
        client.bufferedWriter.newLine();
        client.bufferedWriter.flush();

        // Test sending message
        Scanner scanner = new Scanner(System.in);
        while (client.my_soc.isConnected()) {
            try {
                String msg = scanner.nextLine();
                client.bufferedWriter.write(msg);
                client.bufferedWriter.newLine();
                client.bufferedWriter.flush();
            }  catch (Exception e) {
                client.CloseConnection();
            }
        }
        scanner.close();
    }
}

