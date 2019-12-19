package lab7;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Client {

    private final static String CLIENT_ADDRESS = "tcp://localhost:5555";

    public static void main(String[] args) {
        ZContext context = new ZContext();
        ZMQ.Socket socket = null;

        try {
            socket = context.createSocket(SocketType.REQ);
            socket.connect(CLIENT_ADDRESS);
        } finally {
            context.destroySocket(socket);
            context.destroy();
        }
    }
}
