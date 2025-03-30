package org.woofdb.repl;
import org.woofdb.core.exceptions.MaxTableSizeReachedException;
import org.woofdb.core.exceptions.SyntaxError;
import org.woofdb.core.models.*;
import org.woofdb.core.models.MetaCommand;
import org.woofdb.core.models.statements.InsertStatement;
import org.woofdb.core.models.statements.Statement;
import org.woofdb.core.parser.SQLParser;
import org.woofdb.core.tokenizer.SqlTokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public final class Repl {

    public static void loop() {
        Scanner scanner = new Scanner(System.in);
        Database database = new Database("./woofdb");
        SqlTokenizer sqlTokenizer = new SqlTokenizer();
        SQLParser sqlParser = new SQLParser(sqlTokenizer);
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
                try {
                    final Statement statement = sqlParser.parse(command);
                    switch (executeStatement(table, statement)) {
                        case EXECUTE_TABLE_FULL -> System.out.println("ERROR. Table Full!");
                        case EXECUTE_FAILURE -> System.out.println("Failure while executing statement " + command);
                    }
                }
                catch (SyntaxError e) {
                    System.out.println("SyntaxError: " + e.getMessage());
                }
                catch (IOException e) {
                    System.out.println("IOException: " + e.getMessage());
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

    private static ExecutionResult executeStatement(final Table table, final Statement statement) throws IOException {
        long start = System.currentTimeMillis();
        switch (statement.getStatementType()) {
            case STATEMENT_INSERT -> {
                InsertStatement insertStatement = (InsertStatement) statement;
                Object[] args = insertStatement.getValues().toArray();
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
