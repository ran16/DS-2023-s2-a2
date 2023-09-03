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
        // Create a server socket
        ServerSocket serverSocket = new ServerSocket(7050);

        // Starts the server
        AggregationServer server = new AggregationServer(serverSocket);
        server.StartServer();


    }
}