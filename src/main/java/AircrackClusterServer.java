import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class AircrackClusterServer {

    static int PEER_COUNT;
    static String BSSID;
    static String ESSID;

    static Socket[] sockets = null;
    static int[] clientBenchResult = null;
    static BufferedReader dictionaryFileReader = null;

    static boolean[][] clientState = null;

    // args[0]: peer count
    // args[1]: bssid
    // args[2]: essid
    // args[3]: dictionary File
    // args[4]: cap File

    public static void main(String[] args) throws IOException {

        argsCheck(args);
        dictionaryFileReader = new BufferedReader(new FileReader(new File(args[3])));

        System.out.println("Server Socket Started!\n");
        ServerSocket serverSocket = new ServerSocket(6974);

        sockets = new Socket[PEER_COUNT];
        Thread[] runningThreads = new Thread[PEER_COUNT];

        for (int i = 0; i < PEER_COUNT; i++) {
            Runnable runnable = new ClientInitThread(serverSocket, i);
            runningThreads[i] = new Thread(runnable);
            runningThreads[i].start();
        }

        WaitingThreadForNextCommand(runningThreads);
        runningThreads = new Thread[PEER_COUNT];

        System.out.println("Clients init Completed!");
        System.out.println("Ready for clients bench result!\n");

        clientBenchResult = new int[PEER_COUNT];
        for (int i = 0; i < PEER_COUNT; i++) {
            Runnable runnable = new GetClientBenchResultThread(i);
            runningThreads[i] = new Thread(runnable);
            runningThreads[i].start();
        }

        WaitingThreadForNextCommand(runningThreads);
        runningThreads = new Thread[PEER_COUNT];

        System.out.println("\nSuccessfully Received bench result from all clients!");
        System.out.println("\nReady for sending a captured file to all clients\n");

        File capturedFile = new File((args[4]));
        for (int i = 0; i < PEER_COUNT; i++) {
            Runnable runnable = new SendCapturedFileThread(capturedFile, capturedFile.length(), i);
            runningThreads[i] = new Thread(runnable);
            runningThreads[i].start();
        }

        WaitingThreadForNextCommand(runningThreads);
        runningThreads = new Thread[PEER_COUNT];

        System.out.println("\nSuccessfully sent a captured file to all clients!");
        System.out.println("\nReady for sent AP information to all clients");

        for (int i = 0; i < PEER_COUNT; i++) {
            Runnable runnable = new SendArgsInfoThread(BSSID, ESSID, i);
            runningThreads[i] = new Thread(runnable);
            runningThreads[i].start();
        }

        WaitingThreadForNextCommand(runningThreads);
        runningThreads = new Thread[PEER_COUNT];

        System.out.println("Successfully sent AP information to all clients!");

        initClientState();
        for (int i = 0; i < PEER_COUNT; i++) {
            Runnable runnable = new CrackPasswordThread(i);
            runningThreads[i] = new Thread(runnable);
            runningThreads[i].start();
        }

        int clientAliveCount;
        do {
            clientAliveCount = PEER_COUNT;
            for (int i = 0; i < PEER_COUNT; i++) {
                if (!clientState[i][0]) {
                    if (clientState[i][1]) clientState[i][2] = true;
                } else clientAliveCount--;
            }
        } while (clientAliveCount != 0);
    }

    private static void argsCheck(String[] args) {
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

    private static void initClientState() {
        clientState = new boolean[PEER_COUNT][4];
        for(int i = 0; i < PEER_COUNT; i++)
        {
            clientState[i][0] = false;
            clientState[i][1] = true;
            clientState[i][2] = false;
            clientState[i][3] = false;
        }
    }

    static public void WaitingThreadForNextCommand(Thread[] runningThreads) {
        while (true) {
            int finishedThread = 0;
            for (int i = 0; i < PEER_COUNT; i++)
                if (!runningThreads[i].isAlive()) finishedThread++;
            if (finishedThread == PEER_COUNT) break;
        }
    }
}
