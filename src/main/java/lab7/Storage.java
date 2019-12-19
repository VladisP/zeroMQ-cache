package lab7;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Storage {

    public static void main(String[] args) {
        ZContext context = new ZContext();
        ZMQ.Socket socket = context

        int startCell = Integer.parseInt(args[0]);
        int endCell = Integer.parseInt(args[1]);
    }
}
