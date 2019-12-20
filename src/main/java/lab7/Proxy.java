package lab7;

import javafx.util.Pair;
import org.zeromq.*;

import java.util.*;

import static lab7.CommandService.*;

public class Proxy {

    //private static final List<StorageInfo> storages = new ArrayList<>();
    private static final Map<String, StorageInfo> storages = new HashMap<>();
    private static ZMQ.Socket frontend;
    private static ZMQ.Socket backend;

    private static void updateHeartbeatTime(String id) {
        storages.get(id).setHeartbeatTime(System.currentTimeMillis());
    }

    private static void removeDeadStorages() {
        storages.entrySet().removeIf(entry -> entry.getValue().isDead());
    }

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

        System.out.println("Сервер начал работу");

        while (!Thread.currentThread().isInterrupted()) {
            items.poll(Storage.HEARTBEAT_TIMEOUT);

            if (items.pollin(0)) {
                ZMsg msg = ZMsg.recvMsg(frontend);
                String cmd = new String(msg.getLast().getData(), ZMQ.CHARSET);
                CommandType cmdType = CommandService.getCommandType(cmd);

                if (cmdType == CommandType.GET) {
                    Integer key = CommandService.parseGetCommand(cmd);
                }
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
                    System.out.println("Хранилище зарегистрировано");

                    Pair<Integer, Integer> range = CommandService.parseKeyValueCommand(cmd);

                    storages.put(id, new StorageInfo(
                            address, range.getKey(), range.getValue(), System.currentTimeMillis()
                    ));
                } else if (cmdType == CommandType.NOTIFY) {
                    System.out.println("Обновление времени хартбита");

                    updateHeartbeatTime(id);
                }
            }

            removeDeadStorages();
            System.out.println("Количество живых хранилищ: " + storages.size());
        }

        context.destroySocket(frontend);
        context.destroySocket(backend);
        context.destroy();
    }
}
