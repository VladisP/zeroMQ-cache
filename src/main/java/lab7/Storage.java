package lab7;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

public class Storage {

    public final static String STORAGE_ADDRESS = "tcp://localhost:5556";

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

        long  System.currentTimeMillis()
        sendConnectCommand(socket, startCell, endCell);

        System.out.println("Хранилище настроено");


    }
}
