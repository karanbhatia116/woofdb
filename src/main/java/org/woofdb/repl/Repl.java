package org.woofdb.repl;
import org.woofdb.core.exceptions.MaxTableSizeReachedException;
import org.woofdb.core.models.*;
import org.woofdb.core.models.MetaCommand;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public final class Repl {

    public static void loop() {
        Scanner scanner = new Scanner(System.in);
        Database database = new Database("./woofdb");
        List<Column> columns = List.of(new Column("id", DataType.INT, false),
                new Column("username", DataType.VARCHAR, true),
                new Column("email", DataType.VARCHAR, false)
        );
        Table table;
        try {
            table = database.createTable("users", columns);
        } catch (IOException e) {
            System.out.println("Failed to create table users due to exception " + e);
            return;
        }
        while (true) {
            printPrompt();
            String command = scanner.nextLine();
            command = command.strip();
            if (command.isBlank()) {
                continue;
            }
            if (command.startsWith(".")) {
                MetaCommand metaCommand = MetaCommand.from(command);
                switch (metaCommand) {
                    case HELLO -> System.out.println("Woof!");
                    case EXIT, QUIT -> {
                        System.out.println("Bye!");
                        scanner.close();
                        return;
                    }
                    case HELP -> printManual();
                    case CLEAR -> clearTerminal();
                    default -> System.out.println("Unrecognized command " + command);
                }
            }
            else {
                final Statement statement = new Statement();
                final PrepareStatementResult prepareStatementResult = prepareStatement(command, statement);
                if (prepareStatementResult == PrepareStatementResult.PREPARE_UNRECOGNIZED_STATEMENT) {
                    System.out.println("Unrecognized keyword at start of " + command);
                }
                else if (prepareStatementResult == PrepareStatementResult.PREPARE_SYNTAX_ERROR) {
                    System.out.println("Syntax error. Could not parse statement " + command);
                }
                else {
                    try {
                        switch (executeStatement(table, statement)) {
                            case EXECUTE_TABLE_FULL -> System.out.println("ERROR. Table Full!");
                            case EXECUTE_FAILURE -> System.out.println("Failure while executing statement " + command);
                        }
                    }
                    catch (IOException e) {
                        System.out.println("IOException: " + Arrays.toString(e.getStackTrace()));
                    }
                }
            }
        }
    }

    private static void printPrompt() {
        System.out.print("woof> ");
    }

    private static void printManual() {
        StringBuilder helpText = new StringBuilder();
        helpText.append("\n");
        helpText.append("List of all WoofDB commands:\n");
        helpText.append("Note that all text commands must be first on line and end with ';'\n");

        // Loop through all ShellCommand enum values and add them to the help text
        for (MetaCommand cmd : MetaCommand.values()) {
            // Skip the UNKNOWN command
            if (cmd == MetaCommand.UNKNOWN) {
                continue;
            }

            // Get command and help text
            String command = cmd.getCommand();
            String description = cmd.getHelp();

            // Format the line without shortcuts
            helpText.append(String.format("%-9s %s\n", command, description));
        }

        helpText.append("\n");
        System.out.println(helpText);
    }

    private static void clearTerminal() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    private static PrepareStatementResult prepareStatement(final String command, Statement statement) {
        if (command.toLowerCase().startsWith("insert")) {
            statement.setStatementType(StatementType.STATEMENT_INSERT);
            try {
                String[] parts = command.split(" ");
                if (parts.length < 3) {
                    return PrepareStatementResult.PREPARE_SYNTAX_ERROR;
                }
                int id = Integer.parseInt(parts[1]);
                String username = parts[2];
                String email = parts[3];
                statement.setArgs(id, username, email);
            }
            catch (Exception e) {
                System.out.println("Exception occurred " + e);
                return PrepareStatementResult.PREPARE_SYNTAX_ERROR;
            }
            return PrepareStatementResult.PREPARE_SUCCESS;
        }
        else if (command.toLowerCase().startsWith("select")) {
            statement.setStatementType(StatementType.STATEMENT_SELECT);
            return PrepareStatementResult.PREPARE_SUCCESS;
        }
        return PrepareStatementResult.PREPARE_UNRECOGNIZED_STATEMENT;
    }

    private static ExecutionResult executeStatement(final Table table, final Statement statement) throws IOException {
        long start = System.currentTimeMillis();
        switch (statement.getStatementType()) {
            case STATEMENT_INSERT -> {
                Object[] args = statement.getArgs();
                try {
                    table.addRow(args);
                }
                catch (MaxTableSizeReachedException maxTableSizeReachedException) {
                    return ExecutionResult.EXECUTE_TABLE_FULL;
                }
            }
            case STATEMENT_SELECT -> {
                table.printTableData();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("\nExecuted in " + (end - start) + " ms.");
        return ExecutionResult.EXECUTE_SUCCESS;
    }
}
