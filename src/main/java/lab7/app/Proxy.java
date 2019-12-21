package lab7.app;

import javafx.util.Pair;
import lab7.helpers.CommandService;
import lab7.helpers.StorageInfo;
import org.zeromq.*;

import java.util.*;

import static lab7.helpers.CommandService.*;

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
                msg.send(backend, false);
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
                System.out.println("aaa");
                storageInfo.getAddress().send(backend, ZFrame.REUSE + ZFrame.MORE);
                msg.send(backend, false);
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
                        msg.getLast().reset(CommandService.makeResponseCommand("указанная ячейка вне диапазона кеша"));
                        msg.send(frontend);
                    }
                }

                if (cmdType == CommandType.SET) {
                    System.out.println(cmd); //del this
                    Integer key = CommandService.getKey(cmd);
                    boolean isKeyValid = sendSetRequest(key, msg);

                    String response = isKeyValid ?
                            CommandService.makeResponseCommand("значение записано") :
                            CommandService.makeResponseCommand("указанная ячейка вне диапазона кеша");

                    ZMsg responseMsg = new ZMsg();
                    responseMsg.add(new ZFrame(response));
                    responseMsg.wrap(msg.getFirst());
                    responseMsg.send(frontend);
                }
            }

            if (items.pollin(1)) {
                ZMsg msg = ZMsg.recvMsg(backend);
                ZFrame address = msg.unwrap();
                String id = new String(address.getData(), ZMQ.CHARSET);

                //TODO: be careful!!!
//                String cmd = new String(msg.getFirst().getData(), ZMQ.CHARSET);
                String cmd = new String(msg.getLast().getData(), ZMQ.CHARSET);
                CommandType cmdType = CommandService.getCommandType(cmd);

                if (cmdType == CommandType.CONNECT) {
                    System.out.println("Хранилище зарегистрировано");

                    Pair<Integer, Integer> range = CommandService.getKeyValue(cmd);

                    System.out.println(range.getKey() + " " + range.getValue());

                    storages.put(id, new StorageInfo(
                            address, range.getKey(), range.getValue(), System.currentTimeMillis()
                    ));
                } else if (cmdType == CommandType.NOTIFY) {
//                    System.out.println("Обновление времени хартбита");

                    updateHeartbeatTime(id);
                } else if (cmdType == CommandType.RESPONSE) {
                    msg.send(frontend);
                }
            }

            removeDeadStorages();
//            System.out.println("Количество живых хранилищ: " + storages.size());
        }

        context.destroySocket(frontend);
        context.destroySocket(backend);
        context.destroy();
    }
}
