import java.io.*;
import java.net.Socket;

public class SendCapturedFileThread implements Runnable {
    final private File capturedFile;
    final private long fileLength;
    final private int i;

    public SendCapturedFileThread(File capturedFile, long fileLength, int i) {
        this.capturedFile = capturedFile;
        this.fileLength = fileLength;
        this.i = i;
    }

    public void run() {
        try {
            InputStream input = AircrackClusterServer.sockets[i].getInputStream();
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(input));
            PrintWriter printWriter = new PrintWriter(AircrackClusterServer.sockets[i].getOutputStream(), true);

            String line = socketReader.readLine();

            if(line.contains("FILE_BYTE OK"))
            {
                printWriter.println(fileLength);
                line = socketReader.readLine();
                if(line.contains("FILE_READY OK"))
                {
                    sendCapturedFile(AircrackClusterServer.sockets[i], capturedFile);
                    line = socketReader.readLine();
                    int failCount = 0;
                    if(line.contains("FILE_RECV FAIL"))
                    {
                        while(true)
                        {
                            failCount++;
                            printWriter.println("RETRY");
                            sendCapturedFile(AircrackClusterServer.sockets[i], capturedFile);
                            line = socketReader.readLine();

                            if(line.contains("FILE_RECV FAIL") && failCount == 2)
                            {
                                printWriter.println("DROP");
                                AircrackClusterServer.sockets[i].close();
                            }
                            else if(line.contains("FILE_RECV OK")) break;
                        }
                    }
                }
                System.out.println("Sent a packet file to " + "Client " + (i + 1) + " Successfully!");
            }
        }catch (IOException ignored){}
    }

    public void sendCapturedFile(Socket socket, File capturedFile) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(capturedFile);
        OutputStream socketOutputStream = socket.getOutputStream();

        int readBytes;
        byte[] buffer = new byte[4096];
        while ((readBytes = fileInputStream.read(buffer)) != -1) {
            socketOutputStream.write(buffer, 0, readBytes);
        }
    }
}
