package lab7.app;

import javafx.util.Pair;
import lab7.helpers.CommandService;
import lab7.helpers.StorageInfo;
import org.zeromq.*;

import java.util.*;

import static lab7.helpers.CommandService.*;

public class Proxy {

    private static final List<StorageInfo> storageList = new ArrayList<>();
    private static ZMQ.Socket frontend;
    private static ZMQ.Socket backend;

    private static void updateHeartbeatTime(String id) {
        for (StorageInfo storageInfo : storageList) {
            if (storageInfo.getId().equals(id)) {
                storageInfo.setHeartbeatTime(System.currentTimeMillis());
            }
        }
    }

    private static void removeDeadStorage() {
        storageList.removeIf(StorageInfo::isDead);
    }

    private static boolean sendGetRequest(Integer key, ZMsg msg) {
        for (StorageInfo storageInfo : storageList) {
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

        for (StorageInfo storageInfo : storageList) {
            if (storageInfo.getStart() <= key && key <= storageInfo.getEnd()) {
                storageInfo.getAddress().send(backend, ZFrame.REUSE + ZFrame.MORE);
                msg.send(backend, false);
                isKeyValid = true;
            }
        }

        return isKeyValid;
    }

    public static void main(String[] args) {
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

                String cmd = new String(msg.getLast().getData(), ZMQ.CHARSET);
                CommandType cmdType = CommandService.getCommandType(cmd);

                if (cmdType == CommandType.CONNECT) {
                    Pair<Integer, Integer> range = CommandService.getKeyValue(cmd);

                    storageList.add(new StorageInfo(
                            id, address, range.getKey(), range.getValue(), System.currentTimeMillis()
                    ));

                    System.out.println("Хранилище зарегистрировано");
                } else if (cmdType == CommandType.NOTIFY) {
                    updateHeartbeatTime(id);
                } else if (cmdType == CommandType.RESPONSE) {
                    msg.send(frontend);
                }
            }

            removeDeadStorage();
        }

        context.destroySocket(frontend);
        context.destroySocket(backend);
        context.destroy();
    }
}
