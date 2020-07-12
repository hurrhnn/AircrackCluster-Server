import java.io.*;

public class CrackPasswordThread implements Runnable {

    //public boolean isStop = false; -> clientState[i][0]
    //public boolean wantReader = true; -> clientState[i][1]
    //public boolean canRead = false; -> clientState[i][2]
    //public boolean keyNotFound = false; -> clientState[i][3]

    final private int i;

    public CrackPasswordThread(int i) {
        this.i = i;
    }

    @Override
    public void run() {
        final int THRESHOLD = 60; //60 Sec.
        int tryCount = 0;

        System.out.println("Client " + (i + 1) + ": Send key started!");
        while (!AircrackClusterServer.clientState[i][0]) {
            System.out.println("Client " + (i + 1) + " - trying " + ++tryCount + "...");
            BufferedReader socketReader = null;
            PrintWriter printWriter = null;

            try {
                InputStream input = AircrackClusterServer.sockets[i].getInputStream();
                socketReader = new BufferedReader(new InputStreamReader(input));
                printWriter = new PrintWriter(AircrackClusterServer.sockets[i].getOutputStream(), true);

                if(socketReader.readLine().equals("DICT_SIZE OK")) {
                    printWriter.println(AircrackClusterServer.clientBenchResult[i] * THRESHOLD);
                    if(!socketReader.readLine().equals("DICT_READY OK")) return;
                }
            }catch (Exception ignored) {}

            AircrackClusterServer.clientState[i][1] = true;
            int readCount = 0;
            while (true) {
                if (AircrackClusterServer.clientState[i][2]) {
                    try {
                        if(printWriter != null)
                        {
                            readCount++;
                            if(readCount > AircrackClusterServer.clientBenchResult[i] * THRESHOLD) {
                                String result = socketReader.readLine();
                                if(result.contains("AIRCRACK_RESULT OK")) {
                                    System.out.println(result);
                                    System.exit(0);
                                } else {
                                    AircrackClusterServer.clientState[i][3] = true;
                                    break;
                                }
                            }
                            else printWriter.println(AircrackClusterServer.dictionaryFileReader.readLine());
                        }
                        else break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        AircrackClusterServer.clientState[i][0] = true;
                        break;
                    }
                    AircrackClusterServer.clientState[i][2] = false;
                }
            }
            AircrackClusterServer.clientState[i][1] = false;
            if(AircrackClusterServer.clientState[i][3]) {
                AircrackClusterServer.clientState[i][3] = false;
                continue;
            }
            if (AircrackClusterServer.clientState[i][0]) break;
        }
    }
}
