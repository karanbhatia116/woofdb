package org.woofdb.core.parser;

import org.woofdb.core.exceptions.SyntaxError;
import org.woofdb.core.models.ASTNode;
import org.woofdb.core.models.Token;
import org.woofdb.core.models.TokenType;
import org.woofdb.core.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;

import static org.woofdb.core.models.TokenType.KEYWORD;
import static org.woofdb.core.models.TokenType.SEMICOLON;

public class SQLParser {
    private Tokenizer tokenizer;
    private List<Token> tokens;
    private int position;

    public SQLParser(final Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.tokens = new ArrayList<>();
        this.position = 0;
    }

    public ASTNode parse(final String sql) {
       tokens = tokenizer.getTokens(sql).stream()
                .filter(it -> it.getTokenType() != TokenType.WHITESPACE)
                .toList();

        position = 0;
        return parseSelect(); // based on the first token we need to decide which method to call here for parsing
    }

    private ASTNode parseSelect() {
        ASTNode selectNode = new ASTNode("SELECT_STATEMENT", "");
        expect("SELECT");

        // parse columns
        ASTNode columnsNode = new ASTNode("COLUMNS", "");
        selectNode.addChild(columnsNode);
        do {
            ASTNode columnNode = new ASTNode("COLUMN", getCurrent().getValue());
            columnsNode.addChild(columnNode);
            consume(); // consume column node

            if (position < tokens.size() && getCurrent().getTokenType() == TokenType.COMMA) {
                consume(); // consume the comma
            } else {
                break;
            }
        } while (position < tokens.size());

        expect("FROM");
        ASTNode tableNode = new ASTNode("TABLE", getCurrent().getValue());
        selectNode.addChild(tableNode);
        consume();

        // parse where clause
        if (position < tokens.size()) {
            if (getCurrent().getTokenType() == KEYWORD) {
                if (getCurrent().getValue().equalsIgnoreCase("WHERE") ) {
                    ASTNode whereNode = parseWhere();
                    selectNode.addChild(whereNode);
                }
                else {
                    throw new SyntaxError("Syntax error: Unexpected keyword " + getCurrent().getValue() + " at position " + position + " after table name");
                }
            }
            else if (getCurrent().getTokenType() != SEMICOLON) {
                throw new SyntaxError("Syntax error: Unexpected token at position " + position + " " +
                getCurrent().getTokenType() + " '" + getCurrent().getValue() + "'");
            }
        }

        return selectNode;
    }

    private ASTNode parseWhere() {
        ASTNode whereNode = new ASTNode("WHERE", "");
        consume();
        ASTNode leftSide = new ASTNode("IDENTIFIER", getCurrent().getValue());
        whereNode.addChild(leftSide);
        consume();
        ASTNode operator = new ASTNode("OPERATOR", getCurrent().getValue());
        whereNode.addChild(operator);
        consume();

        ASTNode rightSide = new ASTNode("LITERAL", getCurrent().getValue());
        whereNode.addChild(rightSide);
        consume();
        return whereNode;
    }

    private void expect(final String keywordValue) {
        if (getCurrent().getTokenType() != KEYWORD || !getCurrent().getValue().equalsIgnoreCase(keywordValue)) {
            throw new SyntaxError("Syntax error: Expected " + KEYWORD + " '" + keywordValue + "', found " + getCurrent().getTokenType() + "'" + getCurrent().getValue() + "'");
        }
        consume();
    }

    private Token getCurrent() {
        return tokens.get(position);
    }

    private void consume() {
        position ++;
    }
}

