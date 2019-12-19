package lab7;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Scanner;

import static lab7.CommandService.*;

public class Client {

    private final static String CLIENT_ADDRESS = "tcp://localhost:5555";

    public static void main(String[] args) {
        ZContext context = new ZContext();
        ZMQ.Socket socket = null;
        Scanner in = new Scanner(System.in);

        try {
            socket = context.createSocket(SocketType.REQ);
            socket.connect(CLIENT_ADDRESS);

            while (true) {
                String cmd = in.nextLine();
                CommandType cmdType = CommandService.getCommandType(cmd);

                if (cmdType == CommandType.EXIT) {
                    break;
                }

                
            }
        } finally {
            context.destroySocket(socket);
            context.destroy();
        }
    }
}
