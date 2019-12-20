package lab7;

import org.zeromq.*;

import java.util.Scanner;

import static lab7.CommandService.*;

public class Client {

    public final static String CLIENT_ADDRESS = "tcp://localhost:5555";

    private static void printHelpMessage() {
        System.out.println("Неверная команда, доступны только следующие команды:");
        System.out.println("SET <key> <value>");
        System.out.println("GET <key>");
        System.out.println("F");
    }

    public static void main(String[] args) {
        ZContext context = new ZContext();
        ZMQ.Socket socket = null;
        Scanner in = new Scanner(System.in);

        try {
            socket = context.createSocket(SocketType.REQ);
            socket.connect(CLIENT_ADDRESS);

            System.out.println("Клиент готов к работе, для выхода press F");

            while (true) {
                String cmd = in.nextLine();
                CommandType cmdType = CommandService.getCommandType(cmd);

                if (cmdType == CommandType.EXIT) {
                    break;
                }

                if (cmdType == CommandType.INVALID) {
                    printHelpMessage();
                    continue;
                }

                ZFrame frame = new ZFrame(cmd);
                frame.send(socket, 0);
//                socket.send(cmd, 0);
//                String reply = socket.recvStr(0);
                System.out.println("kek");
                ZMsg msg = ZMsg.recvMsg(socket);
                System.out.println("lol");
                System.out.println(new String(msg.getFirst().getData(), ZMQ.CHARSET));
            }
        } finally {
            context.destroySocket(socket);
            context.destroy();
        }
    }
}
