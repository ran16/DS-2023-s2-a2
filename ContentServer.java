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
    private int LamportClock;
    private Clock clock; // the Clock library

    // This function creates a client object
    public ContentServer(Socket socket) {
        try {
            this.my_soc = socket;
            this.Parser = new Parser();
            this.LamportClock = 0;
            this.clock = new Clock();

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
                this.bufferedReader.close();
            }
            if (bufferedWriter != null) {
                this.bufferedWriter.close();
            }
            if (this.my_soc != null) {
                this.my_soc.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SyncTime() {
        // send a GET request to get weather data
        String header = "GET /time HTTP/1.1\r\n";
        SendMessage(header,"");

        // Recieve the response
        try {
            String response = "";
            String line = this.bufferedReader.readLine();

            while (line != null && !line.isEmpty()) {
                response = response + line + "\n";
                line = bufferedReader.readLine();
            }

            // update clock
            int recieved_time = clock.GetRecievedTime(response);
            this.LamportClock  = (recieved_time > this.LamportClock ) ? recieved_time : this.LamportClock ;
            this.LamportClock++;
        } catch (Exception e) {
            System.out.println("Cannot sync time with server. Closing connection.");
            this.CloseConnection();
        }
    }
    
    // This function sends a PUT request to the Aggregation server to update the latest weather data
    public String UpdateWeather() {
        // send a GET request to get weather data
        String body = this.Parser.txt2JSON(this.FilePath);
        String header = "PUT /weather.json HTTP/1.1\r\n" +
            "User-Agent: ATOMClient/1/0c\r\n" +
            "Content-Type: json\r\n" +
            "Content-Length: " + body.getBytes().length + "\r\n";
        
            // Sync time with the Aggregation server before sending weather update.
        SyncTime();
        SendMessage(header,body);

        System.out.println("Time = " + this.LamportClock+"\n\n");

        // Recieve the response
        try {
            String response = "";
            String line = this.bufferedReader.readLine();

            while (line != null && !line.isEmpty()) {
                response = response + line + "\n";
                line = bufferedReader.readLine();
            }

            // update clock
            int recieved_time = clock.GetRecievedTime(response);
            this.LamportClock  = (recieved_time > this.LamportClock ) ? recieved_time : this.LamportClock ;
            this.LamportClock++;

            return response;
        } catch (Exception e) {
            this.CloseConnection();
            return "";
        }
    }

    public void SendMessage(String header, String body) {
        // Everytime the Content server sends a message, it increase its clock and send the time with the message.
        this.LamportClock++;
        header = header + "Lamport Time: " + this.LamportClock + "\r\n";

        // if there is a body, add line break.
        if (!body.isEmpty()) {
            body = "\r\n" + body;
        }

        if (this.my_soc.isConnected()) {
            try {
                this.bufferedWriter.write(header + body);
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }  catch (Exception e) {
                System.out.println("Something went wrong. The server has disconnected.");
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
            int number_of_updates=0;
            // if the socket has been connected, and the close method has not been called.
            while (cs.my_soc.isConnected() && !cs.my_soc.isClosed() && number_of_updates < 2) {
                System.out.println("Sending weather update "+ number_of_updates +"...");
                String response = cs.UpdateWeather();
                int respons_code = cs.Parser.GetResponseCode(response);

                // If success, sleep for 10 seconds and update again. Otherwise update immediately.
                if ( respons_code >= 200 && respons_code < 300) {
                    number_of_updates++;
                    Thread.sleep(10000);
                } 
            }
        } catch (IOException e) {
            System.out.println("failed to connect to server. Please check the host and port");
            return;
        }
    }
}

