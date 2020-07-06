import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class AircrackClusterServer {

    static int PEER_COUNT;
    static String BSSID;
    static String ESSID;

    static Socket[] sockets = null;
    static Thread[] runningThreads = null;
    static int[] clientBenchResult = null;

    // args[0]: peer count
    // args[1]: bssid
    // args[2]: essid
    // args[3]: dictionary File
    // args[4]: cap File

    public static void main(String[] args) throws IOException {

        argsCheck(args);
        ServerSocket serverSocket = new ServerSocket(6974);

        sockets = new Socket[PEER_COUNT];
        runningThreads = new Thread[PEER_COUNT];

        for (int i = 0; i < PEER_COUNT; i++) {
            Runnable runnable = new ClientInitThread(serverSocket, i);
            runningThreads[i] = new Thread(runnable);
            runningThreads[i].start();
        }

        WaitingThreadForNextCommand();
        System.out.println("Clients init Completed!");

        clientBenchResult = new int[PEER_COUNT];
        for (int i = 0; i < PEER_COUNT; i++) {
            Runnable runnable = new GetClientBenchResultThread(i);
            runningThreads[i] = new Thread(runnable);
            runningThreads[i].start();
        }

        WaitingThreadForNextCommand();
        System.out.println("Successfully Received bench result from all clients!");

        File capturedFile = new File((args[4]));
        for (int i = 0; i < PEER_COUNT; i++) {
            Runnable runnable = new SendCapturedFileThread(capturedFile, capturedFile.length(), i);
            runningThreads[i] = new Thread(runnable);
            runningThreads[i].start();
        }

        WaitingThreadForNextCommand();
        System.out.println("Successfully sent captured file to all clients!");

        /*
        for (int i = 0; i < PEER_COUNT; i++) {
            Runnable runnable = new SendArgsInfoThread(BSSID, ESSID, i);
            runningThreads[i] = new Thread(runnable);
            runningThreads[i].start();
        }

        WaitingThreadForNextCommand();
        System.out.println("Successfully sent AP information to all clients!");
        System.exit(0); */
    }

    public static void argsCheck(String[] args) {
        if (args.length != 0) {
            try {
                PEER_COUNT = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                System.out.println("Peer Count is not a Number. Exit.");
                return;
            }

            if (args.length != 1) {
                int colonCount = 0;
                for (int i = 0; i < args[1].length(); i++) if (args[1].charAt(i) == ':') colonCount++;

                if (colonCount != 5) {
                    System.out.println("It is Not a BSSID. Exit.");
                    return;
                }
                BSSID = args[1];
                if (args.length != 2) {
                    ESSID = args[2];
                    if (args.length != 3) {
                        File dictionaryFile = new File((args[3]));
                        if (!dictionaryFile.exists()) {
                            System.out.println("Dictionary file not founded. Exit.");
                            return;
                        }

                        if (args.length != 4) {
                            File capturedFile = new File((args[4]));
                            if (!capturedFile.exists()) {
                                System.out.println("Dictionary file not founded. Exit.");
                                return;
                            }
                        } else {
                            System.out.println("Captured file needed. Exit.");
                            return;
                        }
                    } else {
                        System.out.println("Dictionary file needed. Exit.");
                        return;
                    }
                } else {
                    System.out.println("ESSID needed. Exit.");
                    return;
                }
            } else {
                System.out.println("BSSID needed. Exit.");
                return;
            }
            System.out.println("All Args Checked!");

        } else {
            System.out.println("Peer Count needed. Exit.");
        }
    }

    static public void WaitingThreadForNextCommand() {
        while (true) {
            int finishedThread = 0;
            for (int i = 0; i < PEER_COUNT; i++)
                if (!runningThreads[i].isAlive()) finishedThread++;
            if (finishedThread == PEER_COUNT) {
                runningThreads = new Thread[PEER_COUNT];
                break;
            }
        }
    }
}
