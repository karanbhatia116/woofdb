package org.woofdb.core.models;

import java.util.Arrays;

public enum MetaCommand {
    HELLO(".hello", "Display a greeting."),
    HELP(".help", "Display this help."),
    EXIT(".exit", "Exit woofdb. Same as quit."),
    CLEAR(".clear", "Clear the terminal."),
    QUIT(".quit", "Quit woofdb."),
    UNKNOWN(null, null);

    private String command;
    private String help;

    MetaCommand(String command, String help) {
        this.command = command;
        this.help = help;
    }

    public String getCommand() {
        return command;
    }

    public String getHelp() {
        return help;
    }

    public static MetaCommand from(String command) {
        return Arrays.stream(MetaCommand.values())
                .filter(it -> it != UNKNOWN && it.getCommand().equals(command))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
