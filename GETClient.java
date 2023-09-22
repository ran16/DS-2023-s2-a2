import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;

public class GETClient {
    private Socket my_soc;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    // This function creates a client object
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

    // This function sends a GET request to the Aggregation server to recieve the latest weather data
    public String GetWeather(String dest) {
        // send a GET request to get weather data
        String msg = "GET /weather HTTP/1.1\n Host:" + dest;
        SendMessage(dest,msg);
        
        String response = "";
        // Recieve the response
        try {
            response = this.bufferedReader.readLine();  
        } catch (Exception e) {
            System.out.println("Something went wrong. Client disconnected.");
            this.CloseConnection();
        }
        return response;
    }

    public void SendMessage(String dest, String msg) {
        if (this.my_soc.isConnected()) {
            try {
                this.bufferedWriter.write(msg);
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }  catch (Exception e) {
                System.out.println("Something went wrong. Client disconnected.");
                this.CloseConnection();
            }
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

        String response = client.GetWeather(url.toString());
        System.out.println(response);

        
    }
}

