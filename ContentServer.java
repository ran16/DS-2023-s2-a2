import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ContentServer {
    private Socket my_soc;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Parser Parser;
    private String FilePath;
    private String ContentServerID;
    private static int LamportClock = 0;

    // This function creates a client object
    public ContentServer(Socket socket) {
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
    
    // This function sends a PUT request to the Aggregation server to update the latest weather data
    public String UpdateWeather(String dest) {
        // send a GET request to get weather data
        String body = this.Parser.txt2JSON(this.FilePath);
        String header = "PUT /weather.json HTTP/1.1\r\n" +
            "User-Agent: ATOMClient/1/0c\r\n" +
            "Content-Type: json\r\n" +
            "Content-Length: " + body.getBytes().length + "\r\n";
        SendMessage(header,body);

        // Recieve the response
        try {
            String response = "";
            String line = this.bufferedReader.readLine();
            while (line != null && !line.isEmpty()) {
                response = response + line + "\n";
                line = bufferedReader.readLine();
            }

            // update clock
            int response_time = GetResponseTime(response);
            ContentServer.LamportClock = (response_time > ContentServer.LamportClock) ? response_time : ContentServer.LamportClock;
            return response;
        } catch (Exception e) {
            this.CloseConnection();
            return "";
        }
    }

    // This function extracts Lamport time from the response recieved.
    public int GetResponseTime(String response) {
        // Define the regular expression pattern to match "Lamport Time: <number>"
        String regex = "Lamport Time: (\\d+)";
        Pattern pattern = Pattern.compile(regex);

        // Use a Matcher to find the pattern in the input string
        Matcher matcher = pattern.matcher(response);

        // Check if the pattern was found
        if (matcher.find()) {
            // Extract the number from the matched group
            String numberStr = matcher.group(1);

            // Parse the number as an integer and return it
            try {
                return Integer.parseInt(numberStr);
            } catch (NumberFormatException e) {
                System.out.println("Time Sync failed. Invalid Lamport Time format: " + numberStr);
                return -1;
            }
        } else {
            // Handle the case where the pattern was not found
            System.out.println("Time Sync failed. Please reconnect.");
            CloseConnection();
            return -1;
        }
    }

    // This function increase the lamport clock by 1
    // Events that trigger clock are: recieving messages, sending messages.
    public synchronized static void tick() {
        LamportClock++;
    }

    public void SendMessage(String header, String body) {
        // Everytime the Content server sends a message, it increase its clock and send the time with the message.
        ContentServer.tick();
        header = header + "Lamport Time: " + AggregationServer.LamportClock + "\r\n";

        // if there is a body, add line break and square brackets.
        if (!body.isEmpty()) {
            body = "\r\n[\n" + body + "\n]";
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
            int i=0;
            // if the socket has been connected, and the close method has not been called.
            while (cs.my_soc.isConnected() && !cs.my_soc.isClosed()) {
                System.out.println("Still connected. \nSending weather update "+i+"...\n");
                String response = cs.UpdateWeather(url.toString());
                int respons_code = cs.Parser.GetResponseCode(response);

                // If success, sleep for 10 seconds and update a gain. Otherwise update immediately.
                if ( respons_code == 200) {
                    i++;
                    Thread.sleep(10000);
                } // else response_code == 201, else 4xx try again, else xxx for empty content
            }
            
        } catch (IOException e) {
            System.out.println("failed to connect to server. Please check the host and port");
            return;
        }
    }
}

