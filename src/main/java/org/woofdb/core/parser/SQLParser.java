package org.woofdb.core.parser;

import org.woofdb.core.exceptions.SyntaxError;
import org.woofdb.core.models.*;
import org.woofdb.core.models.expression.BinaryExpression;
import org.woofdb.core.models.expression.Operation;
import org.woofdb.core.models.expression.StaticValueExpression;
import org.woofdb.core.models.statements.*;
import org.woofdb.core.tokenizer.Tokenizer;

import java.util.*;

import static org.woofdb.core.models.TokenType.*;

public final class SQLParser {
    private final Tokenizer tokenizer;

    public SQLParser(final Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public Statement parse(final String sql) {
        List<Token> tokens = tokenizer.getTokens(sql).stream()
                .filter(it -> it.getTokenType() != TokenType.WHITESPACE)
                .toList();
        int position = 0;
        Token firstToken = tokens.getFirst();
        if (firstToken.getTokenType() == KEYWORD) {
            if (firstToken.getValue().equalsIgnoreCase("SELECT")) {
                return parseSelect(tokens, position); // based on the first token we need to decide which method to call here for parsing
            }
            else if (firstToken.getValue().equalsIgnoreCase("INSERT")) {
                return parseInsert(tokens, position);
            }
            else if (firstToken.getValue().equalsIgnoreCase("CREATE")) {
                return parseCreate(tokens, position);
            }
            else if (firstToken.getValue().equalsIgnoreCase("DROP")) {
                return parseDrop(tokens, position);
            }
            else if (firstToken.getValue().equalsIgnoreCase("UPDATE")) {
                return parseUpdate(tokens, position);
            }
            else if (firstToken.getValue().equalsIgnoreCase("USE")) {
                return parseUse(tokens, position);
            }
            else if (firstToken.getValue().equalsIgnoreCase("SHOW")) {
                return parseShowStatement(tokens, position);
            }
            else  {
                throw new SyntaxError("Illegal token " + firstToken.getValue() + " at position 0");
            }
        }
        else {
            throw new SyntaxError("Statement expected to begin with a keyword. Found " + firstToken.getValue() + " instead");
        }
    }

    private static SelectStatement parseSelect(List<Token> tokens, int position) {
        SelectStatement statement = new SelectStatement();
        expect("SELECT", tokens, position);
        position ++;
        List<String> columns = getExpandedValues(tokens, position);
        statement.setColumns(columns);
        position ++;
        expect("FROM", tokens, position);
        position ++;
        statement.setFrom(getToken(tokens, position).getValue());
        position ++;

        // parse where clause
        if (position < tokens.size()) {
            if (getToken(tokens, position).getTokenType() == KEYWORD) {
                if (getToken(tokens, position).getValue().equalsIgnoreCase("WHERE") ) {
                    statement.setWhere(parseWhere(tokens, position));
                }
                else {
                    throw new SyntaxError("Syntax error: Unexpected keyword " + getToken(tokens, position).getValue() + " at position " + position + " after table name");
                }
            }
            else if (getToken(tokens, position).getTokenType() != SEMICOLON) {
                throw new SyntaxError("Syntax error: Unexpected token at position " + position + " " +
                getToken(tokens, position).getTokenType() + " '" + getToken(tokens, position).getValue() + "'");
            }
        }

        return statement;
    }

    private static InsertStatement parseInsert(List<Token> tokens, int position) {
        InsertStatement statement = new InsertStatement();
        expect("INSERT", tokens, position);
        position ++;
        expect("INTO", tokens, position);
        position ++;
        final String tableName = getToken(tokens, position).getValue();
        statement.setTable(tableName);
        position++;
        List<String> columns = getExpandedValues(tokens, position);
        statement.setColumns(columns);
        position = position + 2 * columns.size() - 1;

        expect("VALUES", tokens, position);
        position ++;

        List<String> values = getExpandedValues(tokens, position);

        statement.setValues(values);
        return statement;
    }

    private static Token getToken(final List<Token> tokens, final int position) {
        if (position >= tokens.size()) {
            throw new SyntaxError("Syntax error at the end of " + position + " token");
        }
        return tokens.get(position);
    }

    private static CreateStatement parseCreate(List<Token> tokens, int position) {
        expect("CREATE", tokens, position);
        position ++;
        ResourceType resourceType = ResourceType.from(getToken(tokens, position).getValue());
        position ++;
        switch (resourceType) {
            case DATABASE -> {
                CreateDatabaseStatement createStatement = new CreateDatabaseStatement();
                String databaseName = getToken(tokens, position).getValue();
                createStatement.setDatabaseName(databaseName);
                return createStatement;
            }
            case TABLE -> {
                CreateTableStatement createStatement = new CreateTableStatement();
                String tableName = getToken(tokens, position).getValue();
                createStatement.setTableName(tableName);
                position ++;
                List<Column> columns = new ArrayList<>();
                do {
                    if (position < tokens.size() && getToken(tokens, position).getTokenType() == TokenType.COMMA) {
                        position++;
                        continue;
                    }
                    else if (position < tokens.size() && getToken(tokens, position).getTokenType() == SEMICOLON) {
                        break;
                    }
                    String columnName = getToken(tokens, position).getValue();
                    Column column = new Column();
                    column.setName(columnName);
                    position ++;
                    column.setDataType(DataType.from(getToken(tokens, position).getValue()));
                    position ++;
                    // TODO: remove this default and handle correctly
                    column.setNullable(true);
                    columns.add(column);
                } while (position < tokens.size());
                createStatement.setColumns(columns);
                return createStatement;
            }
        }
        return null;
    }

    private static DropStatement parseDrop(List<Token> tokens, int position) {
        DropStatement statement = new DropStatement();
        expect("DROP", tokens, position);
        position ++;
        ResourceType resourceType = ResourceType.from(getToken(tokens, position).getValue());
        statement.setResourceType(resourceType);
        position ++;
        String resourceName = getToken(tokens, position).getValue();
        position++;
        statement.setResourceName(resourceName);
        return statement;
    }

    private static UpdateStatement parseUpdate(List<Token> tokens, int position) {
        UpdateStatement statement = new UpdateStatement();
        expect("UPDATE", tokens, position);
        position ++;
        String tableName = getToken(tokens, position).getValue();
        statement.setTableName(tableName);
        position ++;
        expect("SET", tokens, position);
        position ++;
        Map<String, String> updates = new HashMap<>();
        do {
            String columnName = getToken(tokens, position).getValue();
            position += 2; // skip '='
            if (position  >= tokens.size()) {
                throw new SyntaxError("Expected literal value but encountered end of statement at position " + position);
            }
            String literal = getToken(tokens, position).getValue();
            position ++;
            updates.put(columnName, literal);
            if (position < tokens.size() && getToken(tokens, position).getTokenType() == COMMA) {
                position ++;
            } else {
                break;
            }
        } while (position < tokens.size());

        statement.setUpdates(updates);
        // parse where clause
        if (position < tokens.size()) {
            if (getToken(tokens, position).getTokenType() == KEYWORD) {
                if (getToken(tokens, position).getValue().equalsIgnoreCase("WHERE") ) {
                    statement.setCondition(parseWhere(tokens, position));
                }
                else {
                    throw new SyntaxError("Syntax error: Unexpected keyword " + getToken(tokens, position).getValue() + " at position " + position + " after table name");
                }
            }
            else if (getToken(tokens, position).getTokenType() != SEMICOLON) {
                throw new SyntaxError("Syntax error: Unexpected token at position " + position + " " +
                        getToken(tokens, position).getTokenType() + " '" + getToken(tokens, position).getValue() + "'");
            }
        }
        return statement;
    }

    private static UseDatabaseStatement parseUse(List<Token> tokens, int position) {
        expect("USE", tokens, position);
        position ++;
        String databaseName = getToken(tokens, position).getValue();
        position ++;
        UseDatabaseStatement statement = new UseDatabaseStatement();
        statement.setDatabaseName(databaseName);
        return statement;
    }

    private static ShowStatement parseShowStatement(List<Token> tokens, int position) {
        expect("SHOW", tokens, position);
        position ++;
        String resourceType = getToken(tokens, position).getValue();
        position ++;
        if (resourceType.endsWith("s") || resourceType.endsWith("S")) {
            resourceType = resourceType.substring(0, resourceType.length() - 1);
        }
        ResourceType rType = ResourceType.from(resourceType);
        ShowStatement showStatement = new ShowStatement();
        showStatement.setResourceType(rType);
        return showStatement;
    }

    private static BinaryExpression parseWhere(List<Token> tokens, int position) {
        // as of now only parsing a single binary expression
        BinaryExpression binaryExpression = new BinaryExpression();
        position ++;
        binaryExpression.setLeftSide(new StaticValueExpression().withValue(getToken(tokens, position).getValue()));
        position ++;
        binaryExpression.setOperation(Operation.from(getToken(tokens, position).getValue()));
        position ++;

        binaryExpression.setRightSide(new StaticValueExpression().withValue(getToken(tokens, position).getValue()));
        position ++;
        return binaryExpression;
    }

    private static void expect(final String keywordValue, List<Token> tokens, int position) {
        if (position >= tokens.size()) {
            throw new SyntaxError("Expected " + KEYWORD + " '" + keywordValue + "', found end of statement!");
        }
        if (getToken(tokens, position).getTokenType() != KEYWORD || !getToken(tokens, position).getValue().equalsIgnoreCase(keywordValue)) {
            throw new SyntaxError("Syntax error: Expected " + KEYWORD + " '" + keywordValue + "', found " + getToken(tokens, position).getTokenType() + " '" + getToken(tokens, position).getValue() + "' instead at position " + position);
        }
    }

    private static List<String> getExpandedValues(final List<Token> tokens, int position) {
        if (position >= tokens.size()) {
            return Collections.emptyList();
        }
        List<String> values = new ArrayList<>();
        do {
            String value = getToken(tokens, position).getValue();
            values.add(value);
            position++;

            if (position < tokens.size() && getToken(tokens, position).getTokenType() == TokenType.COMMA) {
                position++;
            } else {
                break;
            }
        } while (position < tokens.size());
        return values;
    }
}
