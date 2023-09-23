import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AggregationServer {
    private ServerSocket serverSocket;

    // constructor
    public AggregationServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    // This function starts the server.
    public void StartServer() {
        try {
            System.out.println("The aggregation server has started.");
            
            // Keep listening for connection requests
            while (!serverSocket.isClosed()) {
                // Blocks till a connection request comes through
                Socket client_soc = serverSocket.accept();
                System.out.println("A client has connected.");          

                // Create a client handler to handler the client request
                ClientHandler clientHandler = new ClientHandler(client_soc);

                // Spawn a new thread to handler the client request
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            CloseServerSocket();
        }
    }

    // This function closes the server socket if it is not null.
    public void CloseServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        // Get port number from command line argument
        int port_number = 4567;
        if (args.length > 0) {
            try {
                port_number = Integer.parseInt(args[0]);
            } catch (Exception e){
                port_number = 4567; // the argument is an invalid port number -- default to 4567
            }
        }

        // Create a server socket
        ServerSocket serverSocket = null;
        // Use try-catch to prevent failing to start because the port is not available.
        try {
            serverSocket = new ServerSocket(port_number);
        } catch(Exception e) {
            serverSocket = new ServerSocket(4567);
        }

        // Starts the server
        AggregationServer server = new AggregationServer(serverSocket);
        server.StartServer();


    }
}