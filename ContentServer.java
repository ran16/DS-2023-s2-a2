import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
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
    private String backup_path;

    // This function creates a client object
    public ContentServer(Socket socket) {
        try {
            this.my_soc = socket;
            this.Parser = new Parser();
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
            System.out.println("Quack received time = " + Integer.toString(recieved_time));
            this.LamportClock  = (recieved_time > this.LamportClock ) ? recieved_time : this.LamportClock ;
            this.LamportClock++;
        } catch (Exception e) {
            System.out.println("Cannot sync time with server. Closing connection.");
            this.CloseConnection();
        }
    }
    
    // This function sends a PUT request to the Aggregation server to update the latest weather data
    public String SendWeatherUpdate() {
        // send a GET request to get weather data
        String body = this.Parser.txt2JSON(this.FilePath);
        String header = "PUT /weather.json HTTP/1.1\r\n" +
            "User-Agent: ATOMClient/1/0c\r\n" +
            "Content-Type: json\r\n" +
            "Content-Length: " + body.getBytes().length + "\r\n";
        
        // Sync time with the Aggregation server before sending weather update.
        SyncTime();
        SendMessage(header,body);
        System.out.println("Local Lamport time = " + this.LamportClock);

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

    // This function resends a PUT request recovered from a file
    public String ReSendWeatherUpdate(String msg) {
        // send a GET request to get weather data
        System.out.println(msg);
        
        if (this.my_soc.isConnected()) {
            try {
                this.bufferedWriter.write(msg);
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }  catch (Exception e) {
                System.out.println("Something went wrong. The server has disconnected.");
                CloseConnection();
            }
        }

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

        // Write message to backup
        try (FileOutputStream fos = new FileOutputStream(this.backup_path)) {
            String data = header + body+"\n"; 
            byte[] bytes = data.getBytes();
            fos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this.my_soc.isConnected()) {
            try {
                this.bufferedWriter.write(header + body);
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }  catch (Exception e) {
                System.out.println("Something went wrong. The server has disconnected.");
                CloseConnection();
            }
        }
    }

    public static void main(String args[]) throws IOException, InterruptedException{
        System.out.println("Content server starting...\n");
        
        // Read url from commandline
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

            // Resend PUT request from backup file, if there is one
            try {
                // Load data from backup file
                cs.backup_path = args[2];
                StringBuilder result = new StringBuilder();
        
                try (BufferedReader reader = new BufferedReader(new FileReader(cs.backup_path)) ) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line).append(System.lineSeparator());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // resend old data
                boolean resendingPUTrequest = true;
                while (cs.my_soc.isConnected() && !cs.my_soc.isClosed() && resendingPUTrequest) {
                    System.out.println("Resending PUT request...");
                    String response = cs.ReSendWeatherUpdate(result.toString());

                    int respons_code = cs.Parser.GetResponseCode(response);
                    System.out.println("response code = " + Integer.toString(respons_code) +"\n\n");
                    System.out.println("Quack response = \n" + response +"\n\n");

                    // If success, stop resending and move on to new updates
                    if ( respons_code >= 200 && respons_code < 300) {
                        resendingPUTrequest = false;
                    } 
                } 
            } catch (Exception e) {
                System.out.println("Couldn't load or find backup file for this content server.");
                cs.LamportClock = 0;
            }
            

            // Send PUT request
            int number_of_updates=0;
            // if the socket has been connected, and the close method has not been called.
            while (cs.my_soc.isConnected() && !cs.my_soc.isClosed()) {
                System.out.println("Sending weather update "+ number_of_updates +"...");
                String response = cs.SendWeatherUpdate();
                int respons_code = cs.Parser.GetResponseCode(response);
                System.out.println("response code = " + Integer.toString(respons_code) +"\n\n");
                System.out.println("Quack response = \n" + response +"\n\n");

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

