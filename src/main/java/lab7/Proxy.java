package lab7;

import javafx.util.Pair;
import org.zeromq.*;

import java.util.ArrayList;
import java.util.List;

import static lab7.CommandService.*;

public class Proxy {

    private static final List<StorageInfo> storages = new ArrayList<>();
    private static ZMQ.Socket frontend;
    private static ZMQ.Socket backend;

    public static void main(String[] args) {
        //TODO: try..catch
        ZContext context = new ZContext();
        frontend = context.createSocket(SocketType.ROUTER);
        backend = context.createSocket(SocketType.ROUTER);

        frontend.bind(Client.CLIENT_ADDRESS);
        backend.bind(Storage.STORAGE_ADDRESS);

        ZMQ.Poller items = context.createPoller(2);
        items.register(frontend, ZMQ.Poller.POLLIN);
        items.register(backend, ZMQ.Poller.POLLIN);

        while (!Thread.currentThread().isInterrupted()) {
            items.poll();

            if (items.pollin(0)) {
                //TODO: пока ничего
                ZMsg msg = ZMsg.recvMsg(frontend);
                System.out.println("LEN: " + msg.size());
                System.out.println(msg.toString());
            }

            if (items.pollin(1)) {
                ZMsg msg = ZMsg.recvMsg(backend);
                ZFrame address = msg.unwrap();
                String id = new String(address.getData(), ZMQ.CHARSET);

                //System.out.println("LEN: " + msg.size());
                //System.out.println(id + "|" + msg.toString());
                String cmd = new String(msg.getFirst().getData(), ZMQ.CHARSET);
                CommandType cmdType = CommandService.getCommandType(cmd);

                if (cmdType == CommandType.CONNECT) {
                    Pair<Integer, Integer> 
                }
            }
        }

        context.destroySocket(frontend);
        context.destroySocket(backend);
        context.destroy();
    }
}
