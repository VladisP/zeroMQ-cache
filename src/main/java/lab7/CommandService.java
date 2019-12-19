package lab7;

import java.util.regex.Pattern;

public class CommandService {

    private static Pattern SET_COMMAND_PATTERN = Pattern.compile("SET \\d+ \\d+");
    private static Pattern GET_COMMAND_PATTERN = Pattern.compile("GET \\d+");
    private static Pattern EXIT_COMMAND_PATTERN = Pattern.compile();

    public static enum CommandType {
        GET,
        SET,
        EXIT,
        INVALID
    }

    public static CommandType getCommandType(String cmd) {
        Pattern pattern = Pattern.compile("SET \\d+ \\d+");
    }
}
