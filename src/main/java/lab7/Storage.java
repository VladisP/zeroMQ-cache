package lab7;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

public class Storage {

    private static final int HEARTBEAT_TIMEOUT = 3000;

    public static final String STORAGE_ADDRESS = "tcp://localhost:5556";

    private static void sendConnectCommand(ZMQ.Socket socket, int start, int end) {
        socket.send(CommandService.makeConnectCommand(start, end), 0);
    }

    public static void main(String[] args) {
        //TODO: try..catch
        Map<Integer, Integer> storage = new HashMap<>();

        ZContext context = new ZContext();
        ZMQ.Socket socket = context.createSocket(SocketType.DEALER);
        socket.connect(STORAGE_ADDRESS);

        int startCell = Integer.parseInt(args[0]);
        int endCell = Integer.parseInt(args[1]);

        long heartbeatTime = System.currentTimeMillis() + HEARTBEAT_TIMEOUT;
        sendConnectCommand(socket, startCell, endCell);

        System.out.println("Хранилище настроено");

        while (!Thread.currentThread().isInterrupted()) {
            String cmd = socket.recvStr(0);
            System.out.println(cmd); //TODO: upd that later...

            if (System.currentTimeMillis() >= heartbeatTime) {
                System.out.println("NOTIFY");

                heartbeatTime = System.currentTimeMillis() + HEARTBEAT_TIMEOUT;
            }
        }
    }
}
