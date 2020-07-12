import java.io.*;

public class GetClientBenchResultThread implements Runnable {

    private final int i;

    public GetClientBenchResultThread(int i)
    {
        this.i = i;
    }


    public void run() {
        try {
            InputStream input = AircrackClusterServer.sockets[i].getInputStream();
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(input));
            PrintWriter printWriter = new PrintWriter(AircrackClusterServer.sockets[i].getOutputStream(), true);

            String line = socketReader.readLine();
            int benchResult;
            if(line.contains("BENCH OK"))
            {
                try {
                    benchResult = Integer.parseInt(line.replace("BENCH OK,", ""));
                    AircrackClusterServer.clientBenchResult[i] = benchResult;
                    System.out.println("Client " + (i + 1) + ": " + benchResult + " k/s");
                    printWriter.println("BENCH_RESULT OK");
                }catch (NumberFormatException ignored) {
                    printWriter.println("BENCH_RESULT FAIL");
                }
            }
        }catch (IOException ignored){}
    }
}
