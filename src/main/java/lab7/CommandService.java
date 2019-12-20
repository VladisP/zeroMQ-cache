package lab7;

import javafx.util.Pair;

import java.util.regex.Pattern;

public class CommandService {

    private static final Pattern SET_COMMAND_PATTERN = Pattern.compile("^SET \\d+ \\d+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern GET_COMMAND_PATTERN = Pattern.compile("^GET \\d+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXIT_COMMAND_PATTERN = Pattern.compile("^F$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONNECT_COMMAND_PATTERN = Pattern.compile("^CONNECT \\d+ \\d+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NOTIFY_COMMAND_PATTERN = Pattern.compile("^NOTIFY$");
    private static final String DELIMITER = " ";

    public enum CommandType {
        GET,
        SET,
        EXIT,
        CONNECT,
        NOTIFY,
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
        } else if (NOTIFY_COMMAND_PATTERN.matcher(cmd).find()) {
            return CommandType.NOTIFY;
        } else {
            return CommandType.INVALID;
        }
    }

    public static String makeConnectCommand(int start, int end) {
        return "CONNECT " + start + " " + end;
    }

    public static String makeNotifyCommand() {
        return "NOTIFY";
    }

    private static String[] splitCmd(String cmd) {
        return cmd.split(DELIMITER);
    }

    public static Pair<Integer, Integer> parseConnectCommand(String cmd) {
        String[] cmdParts = splitCmd(cmd);

        return new Pair<>(Integer.parseInt(cmdParts[1]), Integer.parseInt(cmdParts[2]));
    }

    public static Integer parseGetCommand(String cmd) {
        String[] cmdParts = splitCmd(cmd);

        return Integer.parseInt(cmdParts[1]);
    }
}
