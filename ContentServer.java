import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;


public class ContentServer {
    private Socket my_soc;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Parser Parser;
    private String FilePath;
    private String ContentServerID;
    private int LamportClock;

    // This function creates a client object
    public ContentServer(Socket socket) {
        // Initialize lamport clock to be 0
        this.LamportClock = 0;
        try {
            this.my_soc = socket;
            this.Parser = new Parser();
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
    
    // This function sends a PUT request to the Aggregation server to update the latest weather data
    public String UpdateWeather(String dest) {
        // send a GET request to get weather data
        String payload = this.Parser.txt2JSON(this.FilePath);
        String msg = "PUT /weather.json HTTP/1.1\r\n" +
            "User-Agent: ATOMClient/1/0c\r\n" +
            "Content-Type: json\r\n" +
            "Content-Length: " + payload.getBytes().length + "\r\n" +
            "\r\n" + 
            payload + "\n";
        System.out.println(msg);
        SendMessage(dest,msg);

        // Recieve the response
        try {
            String response = "";
            String line = this.bufferedReader.readLine();
            while (line != null && !line.isEmpty()) {
                response = response + line + "\n";
                line = bufferedReader.readLine();
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void SendMessage(String dest, String msg) {
        if (this.my_soc.isConnected()) {
            try {
                this.bufferedWriter.write(msg);
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }  catch (Exception e) {
                e.printStackTrace();
                System.out.println("Something went wrong. Client disconnected.");
                this.CloseConnection();
            }
        }
    }

    public static void main(String args[]) throws IOException, InterruptedException{
        if (args.length < 1) {
            System.out.println("Please provide valid url.");
            return;
        }
        
        // Get the server name and port from commandline
        URL url = null;
        String host = "";
        int port;
        try {
            url = new URL(args[0]);
            host = url.getHost();
            port = url.getPort();
        } catch (Exception e){
            System.out.println("Please provide valid url.");
            return;
        }

        // Connect
        try{
            Socket socket = new Socket(host, port);
            ContentServer cs = new ContentServer(socket);
            System.out.println("Content Server is connected");
            
            // Get file path to the weather
            cs.FilePath = args[1];

            // Send PUT request
            while (cs.my_soc.isConnected()) {
                System.out.println("Sending weather update...\n");
                String response = cs.UpdateWeather(url.toString());
                int respons_code = cs.Parser.GetResponseCode(response);

                // If success, sleep for 10 seconds and update a gain. Otherwise update immediately.
                if ( respons_code == 200) {
                    Thread.sleep(10000);
                } 
            }
            
        } catch (IOException e) {
            System.out.println("failed to connect to server. Please check the host and port");
            return;
        }
    }
}

