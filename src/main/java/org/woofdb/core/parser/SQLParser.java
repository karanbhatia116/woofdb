package org.woofdb.core.parser;

import org.woofdb.core.exceptions.SyntaxError;
import org.woofdb.core.models.*;
import org.woofdb.core.models.expression.BinaryExpression;
import org.woofdb.core.models.expression.Operation;
import org.woofdb.core.models.expression.StaticValueExpression;
import org.woofdb.core.models.statements.*;
import org.woofdb.core.tokenizer.SqlTokenizer;
import org.woofdb.core.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.woofdb.core.models.TokenType.KEYWORD;
import static org.woofdb.core.models.TokenType.SEMICOLON;

public class SQLParser {
    private Tokenizer tokenizer;

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
        statement.setFrom(tokens.get(position).getValue());
        position ++;

        // parse where clause
        if (position < tokens.size()) {
            if (tokens.get(position).getTokenType() == KEYWORD) {
                if (tokens.get(position).getValue().equalsIgnoreCase("WHERE") ) {
                    statement.setWhere(parseWhere(tokens, position));
                }
                else {
                    throw new SyntaxError("Syntax error: Unexpected keyword " + tokens.get(position).getValue() + " at position " + position + " after table name");
                }
            }
            else if (tokens.get(position).getTokenType() != SEMICOLON) {
                throw new SyntaxError("Syntax error: Unexpected token at position " + position + " " +
                tokens.get(position).getTokenType() + " '" + tokens.get(position).getValue() + "'");
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
        final String tableName = tokens.get(position).getValue();
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

    private static CreateStatement parseCreate(List<Token> tokens, int position) {
        expect("CREATE", tokens, position);
        position ++;
        ResourceType resourceType = ResourceType.from(tokens.get(position).getValue());
        position ++;
        switch (resourceType) {
            case DATABASE -> {
                CreateDatabaseStatement createStatement = new CreateDatabaseStatement();
                String databaseName = tokens.get(position).getValue();
                createStatement.setDatabaseName(databaseName);
                return createStatement;
            }
            case TABLE -> {
                CreateTableStatement createStatement = new CreateTableStatement();
                String tableName = tokens.get(position).getValue();
                createStatement.setTableName(tableName);
                position ++;
                List<Column> columns = new ArrayList<>();
                do {
                    if (position < tokens.size() && tokens.get(position).getTokenType() == TokenType.COMMA) {
                        position++;
                        continue;
                    }
                    else if (position < tokens.size() && tokens.get(position).getTokenType() == SEMICOLON) {
                        break;
                    }
                    String columnName = tokens.get(position).getValue();
                    Column column = new Column();
                    column.setName(columnName);
                    position ++;
                    column.setDataType(DataType.from(tokens.get(position).getValue()));
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
        ResourceType resourceType = ResourceType.from(tokens.get(position).getValue());
        statement.setResourceType(resourceType);
        position ++;
        String resourceName = tokens.get(position).getValue();
        position++;
        statement.setResourceName(resourceName);
        return statement;
    }

    private static SelectStatement parseUpdate(List<Token> tokens, int position) {
        SelectStatement statement = new SelectStatement();
        expect("SELECT", tokens, position);
        position ++;
        List<String> columns = getExpandedValues(tokens, position);

        statement.setColumns(columns);
        expect("FROM", tokens, position);
        position ++;
        statement.setFrom(tokens.get(position).getValue());
        position ++;

        // parse where clause
        if (position < tokens.size()) {
            if (tokens.get(position).getTokenType() == KEYWORD) {
                if (tokens.get(position).getValue().equalsIgnoreCase("WHERE") ) {
                    statement.setWhere(parseWhere(tokens, position));
                }
                else {
                    throw new SyntaxError("Syntax error: Unexpected keyword " + tokens.get(position).getValue() + " at position " + position + " after table name");
                }
            }
            else if (tokens.get(position).getTokenType() != SEMICOLON) {
                throw new SyntaxError("Syntax error: Unexpected token at position " + position + " " +
                        tokens.get(position).getTokenType() + " '" + tokens.get(position).getValue() + "'");
            }
        }

        return statement;
    }

    private static BinaryExpression parseWhere(List<Token> tokens, int position) {
        // as of now only parsing a single binary expression
        BinaryExpression binaryExpression = new BinaryExpression();
        position ++;
        binaryExpression.setLeftSide(new StaticValueExpression().withValue(tokens.get(position).getValue()));
        position ++;
        binaryExpression.setOperation(Operation.from(tokens.get(position).getValue()));
        position ++;

        binaryExpression.setRightSide(new StaticValueExpression().withValue(tokens.get(position).getValue()));
        position ++;
        return binaryExpression;
    }

    private static void expect(final String keywordValue, List<Token> tokens, int position) {
        if (position >= tokens.size()) {
            throw new SyntaxError("Expected " + KEYWORD + " '" + keywordValue + "', found end of statement!");
        }
        if (tokens.get(position).getTokenType() != KEYWORD || !tokens.get(position).getValue().equalsIgnoreCase(keywordValue)) {
            throw new SyntaxError("Syntax error: Expected " + KEYWORD + " '" + keywordValue + "', found " + tokens.get(position).getTokenType() + " '" + tokens.get(position).getValue() + "' instead at position " + position);
        }
    }

    private static List<String> getExpandedValues(final List<Token> tokens, int position) {
        if (position >= tokens.size()) {
            return Collections.emptyList();
        }
        List<String> values = new ArrayList<>();
        do {
            String value = tokens.get(position).getValue();
            values.add(value);
            position++;

            if (position < tokens.size() && tokens.get(position).getTokenType() == TokenType.COMMA) {
                position++;
            } else {
                break;
            }
        } while (position < tokens.size());
        return values;
    }
}
