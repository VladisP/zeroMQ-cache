package lab7;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandService {

    private static Pattern SET_COMMAND_PATTERN = Pattern.compile("SET \\d+ \\d+", Pattern.CASE_INSENSITIVE);
    private static Pattern GET_COMMAND_PATTERN = Pattern.compile("GET \\d+", Pattern.CASE_INSENSITIVE);
    private static Pattern EXIT_COMMAND_PATTERN = Pattern.compile("F", Pattern.CASE_INSENSITIVE);

    public enum CommandType {
        GET,
        SET,
        EXIT,
        INVALID
    }

    public static CommandType getCommandType(String cmd) {
        Matcher matcher = 
    }
}
