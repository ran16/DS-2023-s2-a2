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
    private Parser Parser; // create a Parser for string <-> JSON
    

    // This function creates a client object
    public GETClient(Socket socket) {
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

    // This function sends a GET request to the Aggregation server to recieve the latest weather data
    public String GetWeather(String dest, String stationID) {
        // send a GET request to get weather data
        String msg = "GET /weather" + stationID + " HTTP/1.1\nHost:" + dest+"\n";
        SendMessage(dest,msg);

        // Recieve the response
        try {
            String response = "";
            String line = this.bufferedReader.readLine(); 
            while (line != null ) {
                response = response + line + "\n";
                line = bufferedReader.readLine();
            }
            return response;
        } catch (Exception e) {
            return "";
        }
    }

    // This function returns the response code as an int
    public int GetResponseCode(String response) {
        return Integer.parseInt(response.split(" ")[1]);
    }

    // This function prints the weather from the response
    public void PrintWeather(String response) {
        // Find where the JSON content starts by finding the index of the first {
        int i=0;
        while (response.charAt(i) != '{') {
            i++;
        }
        System.out.println(Parser.JSON2String(response.substring(i, response.length()-1)));
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
            GETClient client = new GETClient(socket);
            System.out.println("Client is connected");

            // Get station ID if there is one
            String stationID = "";
            if (args.length > 1) {
                stationID = "/"+args[1];
            }

            // Send GET request
            String response = client.GetWeather(url.toString(), stationID);
            try {
                int respons_code = client.Parser.GetResponseCode(response);
                if (respons_code == 200) {
                    System.out.println("\nWeather " + stationID + ":\n");
                    client.PrintWeather(response);
                } else {
                    System.out.println(respons_code);
                    client.CloseConnection();
                }
            } catch (Exception e) {
                System.out.println("Did not get valid response. Please try again.");
                client.CloseConnection();
            }
        } catch (IOException e) {
            System.out.println("failed to connect to server. Please check the host and port");
            return;
        }
    }
}

