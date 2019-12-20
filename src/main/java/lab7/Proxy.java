package lab7;

import javafx.util.Pair;
import org.zeromq.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

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
        storages.forEach((s, storageInfo) -> {
            if (storageInfo.isDead()) {
                System.out.println("Удалено мертвое хранилище");
                storages.remove(s);
            }
        });
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
            System.out.println("iter");
            items.poll(Storage.HEARTBEAT_TIMEOUT);

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
                    System.out.println("Хранилище зарегистрировано");

                    Pair<Integer, Integer> range = CommandService.parseConnectCommand(cmd);

                    storages.put(id, new StorageInfo(
                            address, range.getKey(), range.getValue(), System.currentTimeMillis()
                    ));
                } else if (cmdType == CommandType.NOTIFY) {
                    System.out.println("Обновление времени хартбита");

                    updateHeartbeatTime(id);
                }
            }

            removeDeadStorages();
        }

        context.destroySocket(frontend);
        context.destroySocket(backend);
        context.destroy();
    }
}
