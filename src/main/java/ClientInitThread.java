import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientInitThread implements Runnable {

    final ServerSocket serverSocket;
    final int i;

    public ClientInitThread(ServerSocket serverSocket, int i)
    {
        this.serverSocket = serverSocket;
        this.i = i;
    }

    public void run()
    {
        System.out.println("Server Socket Started!\n");
        try {
            Socket socket = serverSocket.accept();
            AircrackClusterServer.sockets[i] = socket;
            System.out.println(i + ": Socket Connected!\n");
        }catch (IOException ignored) {}
    }
}
