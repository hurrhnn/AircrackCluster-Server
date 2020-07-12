import java.io.*;

public class SendArgsInfoThread implements Runnable {

    final private String BSSID;
    final private String ESSID;
    final private int i;

    public SendArgsInfoThread(String BSSID, String ESSID, int i) {
        this.BSSID = BSSID;
        this.ESSID = ESSID;
        this.i = i;
    }

    public void run() {
        try {
            InputStream input = AircrackClusterServer.sockets[i].getInputStream();
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(input));
            PrintWriter printWriter = new PrintWriter(AircrackClusterServer.sockets[i].getOutputStream(), true);

            String line = socketReader.readLine();
            if (line.contains("INF_READY OK")) printWriter.println(BSSID + "," + ESSID);

            System.out.println("Client " + (i + 1) + ": sent AP information Successfully!");
        } catch (IOException ignored) {}
    }
}
