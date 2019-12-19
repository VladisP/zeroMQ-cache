package lab7;

import java.util.regex.Pattern;

public class CommandService {

    private static Pattern SET_COMMAND_PATTERN = Pattern.compile("^SET \\d+ \\d+$", Pattern.CASE_INSENSITIVE);
    private static Pattern GET_COMMAND_PATTERN = Pattern.compile("^GET \\d+$", Pattern.CASE_INSENSITIVE);
    private static Pattern EXIT_COMMAND_PATTERN = Pattern.compile("^F$", Pattern.CASE_INSENSITIVE);
    private static Pattern CONNECT_COMMAND_PATTERN = Pattern.compile("^CONNECT \\d+ \\d+$", Pattern.CASE_INSENSITIVE);

    public enum CommandType {
        GET,
        SET,
        EXIT,
        CONNECT,
        INVALID
    }

    public static CommandType getCommandType(String cmd) {
        if (SET_COMMAND_PATTERN.matcher(cmd).find()) {
            return CommandType.SET;
        } else if (GET_COMMAND_PATTERN.matcher(cmd).find()) {
            return CommandType.GET;
        } else if (EXIT_COMMAND_PATTERN.matcher(cmd).find()) {
            return CommandType.EXIT;
        } else if (CONNECT_COMMAND_PATTERN.matcher(cmd).find()) {
            return CommandType.CONNECT;
        } else {
            return CommandType.INVALID;
        }
    }

    public static String makeConnectCommand(int start, int end) {
        return "CONNECT " + start + " " + end;
    }
}
