import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CrackPasswordThread implements Runnable {

    //public boolean isStop = false; -> clientState[i][0]
    //public boolean wantReader = true; -> clientState[i][1]
    //public boolean canRead = false; -> clientState[i][2]

    final private int i;

    public CrackPasswordThread(int i) {
        this.i = i;
    }

    @Override
    public void run() {

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(new File(Thread.currentThread().getName()+".txt")));
        } catch (IOException ignored) { }

        while (!AircrackClusterServer.clientState[i][0]) {
            AircrackClusterServer.clientState[i][1] = true;
            while (true) {
                if (AircrackClusterServer.clientState[i][2]) {
                    try {
                        if(bw != null) bw.write(AircrackClusterServer.dictionaryFileReader.readLine());
                        else break;

                    } catch (IOException e) {
                        e.printStackTrace();
                        AircrackClusterServer.clientState[i][0] = true;
                        break;
                    }
                    AircrackClusterServer.clientState[i][2] = false;
                    break;
                }
            }
            AircrackClusterServer.clientState[i][1] = false;
            if (AircrackClusterServer.clientState[i][0]) break;
        }
    }
}
