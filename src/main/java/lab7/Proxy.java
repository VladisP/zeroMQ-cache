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

    private static boolean sendGetRequest(Integer key, ZMsg msg) {
        for (Map.Entry<String, StorageInfo> entry : storages.entrySet()) {
            StorageInfo storageInfo = entry.getValue();

            if (storageInfo.getStart() <= key && key <= storageInfo.getEnd()) {
                storageInfo.getAddress().send(backend, ZFrame.REUSE + ZFrame.MORE);
                msg.send(backend);
                return true;
            }
        }

        return false;
    }

    private static boolean sendSetRequest(Integer key, ZMsg msg) {
        boolean isKeyValid = false;

        for (Map.Entry<String, StorageInfo> entry : storages.entrySet()) {
            StorageInfo storageInfo = entry.getValue();

            if (storageInfo.getStart() <= key && key <= storageInfo.getEnd()) {
                storageInfo.getAddress().send(backend, ZFrame.REUSE + ZFrame.MORE);
                msg.send(backend);
                isKeyValid = true;
            }
        }

        return isKeyValid;
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
                    Integer key = CommandService.getKey(cmd);
                    boolean isKeyValid = sendGetRequest(key, msg);

                    if (!isKeyValid) {
                        msg.getLast().reset(CommandService.makeResponse("указанная ячейка вне диапазона кеша"));
                        msg.send(frontend);
                    }
                }

                if (cmdType == CommandType.SET) {
                    Integer key = CommandService.getKey(cmd);
                    boolean isKeyValid = sendSetRequest(key, msg);

                    String response = isKeyValid ?
                            CommandService.makeResponse("значение записано") :
                            CommandService.makeResponse("указанная ячейка вне диапазона кеша");

                    
                    msg.getLast().reset(response);
                    msg.send(frontend);
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

                    Pair<Integer, Integer> range = CommandService.getKeyValue(cmd);

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
