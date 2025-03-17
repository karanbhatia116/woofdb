package org.woofdb.core.models;

public enum TokenType {
    KEYWORD, // reserved words like SELECT, INSERT, INTO, etc.
    IDENTIFIER, // Table/Column names
    OPERATOR, // =, >, <, etc.
    LITERAL, // String or numeric values
    COMMA, // ,
    SEMICOLON, // ;
    WHITESPACE; // spaces, tabs, new lines
}
