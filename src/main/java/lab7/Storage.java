package lab7;

import javafx.util.Pair;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.HashMap;
import java.util.Map;

import static lab7.CommandService.*;

public class Storage {

    public static final int HEARTBEAT_TIMEOUT = 3000;
    public static final String STORAGE_ADDRESS = "tcp://localhost:5556";

    private static void sendConnectCommand(ZMQ.Socket socket, int start, int end) {
        socket.send(makeConnectCommand(start, end), 0);
    }

    private static void sendNotifyCommand(ZMQ.Socket socket) {
        socket.send(makeNotifyCommand(), 0);
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
//            String cmd = socket.recvStr(ZMQ.DONTWAIT);
            ZMsg msg = ZMsg.recvMsg(socket, false);

            if (msg != null) {
                String cmd = new String(msg.getLast().getData(), ZMQ.CHARSET);
                CommandType cmdType = CommandService.getCommandType(cmd);

                System.out.println(cmd);

                if (cmdType == CommandType.GET) {
                    Integer key = CommandService.parseGetCommand(cmd);

                    msg.getLast().reset(Integer.toString(storage.get(key)));
                    msg.send(socket);
                }

                if (cmdType == CommandType.SET) {
                    Pair<Integer, Integer> setParams = CommandService.parseKeyValueCommand(cmd);

                    storage.put(setParams.getKey(), setParams.getValue());
                    msg.destroy();
                }
            }

            if (System.currentTimeMillis() >= heartbeatTime) {
                System.out.println("NOTIFY");

                heartbeatTime = System.currentTimeMillis() + HEARTBEAT_TIMEOUT;
                sendNotifyCommand(socket);
            }
        }

        context.destroySocket(socket);
        context.destroy();
    }
}
