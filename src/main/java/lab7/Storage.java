package lab7;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Storage {

    public final static String STORAGE_ADDRESS = ""

    public static void main(String[] args) {
        ZContext context = new ZContext();
        ZMQ.Socket socket = context.createSocket(SocketType.DEALER);
        socket.connect()

        int startCell = Integer.parseInt(args[0]);
        int endCell = Integer.parseInt(args[1]);
    }
}
