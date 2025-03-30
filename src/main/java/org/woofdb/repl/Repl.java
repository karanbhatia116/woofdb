package org.woofdb.repl;
import org.woofdb.core.exceptions.MaxTableSizeReachedException;
import org.woofdb.core.exceptions.SyntaxError;
import org.woofdb.core.models.*;
import org.woofdb.core.models.MetaCommand;
import org.woofdb.core.models.statements.*;
import org.woofdb.core.parser.SQLParser;
import org.woofdb.core.tokenizer.SqlTokenizer;
import org.woofdb.core.tokenizer.Tokenizer;

import java.io.IOException;
import java.util.Scanner;

public final class Repl {

    private Database currentDatabase = null;

    public void loop() {
        Scanner scanner = new Scanner(System.in);
        Tokenizer sqlTokenizer = new SqlTokenizer();
        SQLParser sqlParser = new SQLParser(sqlTokenizer);
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
                    switch (executeStatement(statement)) {
                        case EXECUTE_TABLE_FULL -> System.out.println("ERROR. Table Full!");
                        case EXECUTE_FAILURE -> System.out.println("Failure while executing statement " + command);
                    }
                }
                catch (SyntaxError e) {
                    System.out.println("SyntaxError: " + e.getMessage());
                }
                catch (IOException e) {
                    System.out.println("IOException: " + e.getMessage());
                } catch (MaxTableSizeReachedException e) {
                    throw new RuntimeException(e);
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

    private ExecutionResult executeStatement(final Statement statement) throws IOException, MaxTableSizeReachedException {
        long start = System.currentTimeMillis();
        switch (statement.getStatementType()) {
            case STATEMENT_INSERT -> {
                if (currentDatabase == null) {
                    return noDbSelectedResult();
                }
                InsertStatement insertStatement = (InsertStatement) statement;
                String tableName = insertStatement.getTable();
                Table table = currentDatabase.getTable(tableName);
                if (table == null) {
                    return noSuchTableResult(tableName);
                }
                Object[] args = insertStatement.getValues().toArray();
                try {
                    table.addRow(args);
                }
                catch (MaxTableSizeReachedException maxTableSizeReachedException) {
                    return ExecutionResult.EXECUTE_TABLE_FULL;
                }
            }
            case STATEMENT_SELECT -> {
                if (currentDatabase == null) {
                    return noDbSelectedResult();
                }
                SelectStatement selectStatement = (SelectStatement) statement;
                String tableName = selectStatement.getFrom();
                Table table = currentDatabase.getTable(tableName);
                if (table == null) {
                    return noSuchTableResult(tableName);
                }
                table.printTableData();
            }
            case STATEMENT_USE -> {
                UseDatabaseStatement useDatabaseStatement = (UseDatabaseStatement) statement;
                String databaseName = useDatabaseStatement.getDatabaseName();
                currentDatabase = new Database(getBaseDatabaseDirectoryPath() + databaseName);
            }
            case STATEMENT_CREATE -> {
                if (statement instanceof CreateDatabaseStatement createDatabaseStatement) {
                    Database database = new Database(getBaseDatabaseDirectoryPath() + createDatabaseStatement.getDatabaseName());
                    database.loadTables();
                }
                else if (statement instanceof CreateTableStatement createTableStatement) {
                    if (currentDatabase == null) {
                        return noDbSelectedResult();
                    }
                    currentDatabase.createTable(createTableStatement.getTableName(), createTableStatement.getColumns());
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("\nExecuted in " + (end - start) + " ms.");
        return ExecutionResult.EXECUTE_SUCCESS;
    }

    private static String getBaseDatabaseDirectoryPath() {
        return "./db/";
    }

    private static ExecutionResult noDbSelectedResult() {
        System.out.println("No database selected.");
        return ExecutionResult.EXECUTE_FAILURE;
    }

    private static ExecutionResult noSuchTableResult(String tableName) {
        System.out.println("Unknown table '" + tableName + "'");
        return ExecutionResult.EXECUTE_FAILURE;
    }
}
