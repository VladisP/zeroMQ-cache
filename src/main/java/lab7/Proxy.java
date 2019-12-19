package lab7;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.List;

public class Proxy {

    private static final List<StorageInfo> storages = new ArrayList<>();
    private static ZMQ.Socket frontend;
    private static ZMQ.Socket backend;

    public static void main(String[] args) {
        //TODO: try..catch
        ZContext context = new ZContext();
        frontend = context.createSocket(SocketType.ROUTER);
    }
}
