public class Client {
    private Socket my_soc;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public Client(Socket socket) {
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
            if (client_soc != null) {
                my_soc.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        // Get the server name and port from commandline
        URL url = new URL(args[0]);
        String host = url.getHost();
        int port = url.getPort();

        System.out.println("Client is connected")

        Scanner scanner = new Scanner(System.in);
        
    }
}

