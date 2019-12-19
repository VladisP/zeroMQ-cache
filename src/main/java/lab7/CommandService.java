package lab7;

import java.util.regex.Pattern;

public class CommandService {

    public static enum CommandType {
        GET,
        SET,
        EXIT,
        INVALID
    }

    public static CommandType getCommandType(String cmd) {
        Pattern pattern = Pattern.compile("SET \d");
    }
}
