package lab7;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Scanner;

public class Client {

    private final static String CLIENT_ADDRESS = "tcp://localhost:5555";

    private static 

    public static void main(String[] args) {
        ZContext context = new ZContext();
        ZMQ.Socket socket = null;
        Scanner in = new Scanner(System.in);

        try {
            socket = context.createSocket(SocketType.REQ);
            socket.connect(CLIENT_ADDRESS);

            while (true) {
                String cmd = in.nextLine();
            }
        } finally {
            context.destroySocket(socket);
            context.destroy();
        }
    }
}
