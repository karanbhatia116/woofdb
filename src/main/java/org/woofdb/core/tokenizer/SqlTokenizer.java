package org.woofdb.core.tokenizer;

import org.woofdb.core.exceptions.SyntaxError;
import org.woofdb.core.models.Token;
import org.woofdb.core.models.TokenType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class SqlTokenizer implements Tokenizer {

    public static Set<String> SUPPORTED_KEYWORDS = Set.of(
            "AND",
            "CREATE",
            "DATABASE",
            "DATABASES",
            "DESCRIBE",
            "DROP",
            "EXPLAIN",
            "FROM",
            "INDEX",
            "INSERT",
            "INTO",
            "LIKE",
            "LIMIT",
            "ON",
            "OR",
            "PRIMARY",
            "SELECT",
            "SET",
            "SHOW",
            "TABLE",
            "TABLES",
            "UPDATE",
            "USE",
            "VALUES",
            "WHERE"
    );

    public static boolean isKeyword(String word) {
        return SUPPORTED_KEYWORDS.contains(word.toUpperCase());
    }

    @Override
    public List<Token> getTokens(final String sql) {

        if (sql == null || sql.isBlank())
            return Collections.emptyList();

        final List<Token> tokens = new ArrayList<>();
        int position = 0;

        while (position < sql.length()) {
            char currentChar = sql.charAt(position);

            // skip whitespaces
            if (Character.isWhitespace(currentChar)) {
                int startPosition = position;
                while (position < sql.length() && Character.isWhitespace(sql.charAt(position))) {
                        position ++;
                }
                tokens.add(new Token(TokenType.WHITESPACE, sql.substring(startPosition, position)));
                continue;
            }

            // check for identifiers and keywords
            if (Character.isLetter(currentChar)) {
                int startPosition = position;
                while (position < sql.length() && (Character.isLetterOrDigit(sql.charAt(position)) || sql.charAt(position) == '_')) {
                    position++;
                }

                String word = sql.substring(startPosition, position);
                if (isKeyword(word)) {
                    tokens.add(new Token(TokenType.KEYWORD, word));
                }
                else {
                    tokens.add(new Token(TokenType.IDENTIFIER, word));
                }
                continue;
            }

            // check for *
            if (currentChar == '*') {
                tokens.add(new Token(TokenType.IDENTIFIER, Character.toString(currentChar)));
                position++;
                continue;
            }

            // check for comments
            if (currentChar == '-') {
                int nextPosition = position + 1;
                if (nextPosition < sql.length() && sql.charAt(nextPosition) == '-') {
                    tokens.add(new Token(TokenType.COMMENT, sql.substring(position)));
                }
                return tokens;
            }

            // check for string literals
            if (currentChar == '\'') {
                int startPosition = position;
                do {
                    position++;
                } while (position < sql.length() && sql.charAt(position) != '\'');

                if (position < sql.length()) {
                    position ++;
                }

                tokens.add(new Token(TokenType.LITERAL, sql.substring(startPosition, position)));
                continue;
            }

            // check for numeric literals
            if (Character.isDigit(currentChar)) {
                int startPosition = position;
                while (position < sql.length()
                        && (Character.isDigit(sql.charAt(position)) || sql.charAt(position) == '.')) {
                    position++;
                }
                tokens.add(new Token(TokenType.LITERAL, sql.substring(startPosition, position)));
                continue;
            }

            if (currentChar == '=' || currentChar == '>' || currentChar == '<') {
                tokens.add(new Token(TokenType.OPERATOR, Character.toString(currentChar)));
                position ++;
                continue;
            }

            if (currentChar == ',') {
                tokens.add(new Token(TokenType.COMMA, Character.toString(currentChar)));
                position ++;
                continue;
            }

            if (currentChar == ';') {
                tokens.add(new Token(TokenType.SEMICOLON, Character.toString(currentChar)));
                position++;
                continue;
            }

            if (currentChar == '(' || currentChar == ')') {
                position ++;
            }

            else {
                throw new SyntaxError("Invalid character " + currentChar + " at position " + position);
            }
        }

        return tokens;
    }
}
