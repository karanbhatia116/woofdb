package org.woofdb.core.parser;

import org.woofdb.core.exceptions.SyntaxError;
import org.woofdb.core.models.ASTNode;
import org.woofdb.core.models.Token;
import org.woofdb.core.models.TokenType;
import org.woofdb.core.tokenizer.Tokenizer;
import java.util.List;

import static org.woofdb.core.models.TokenType.KEYWORD;
import static org.woofdb.core.models.TokenType.SEMICOLON;

public class SQLParser {
    private Tokenizer tokenizer;

    public SQLParser(final Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public ASTNode parse(final String sql) {
        List<Token> tokens = tokenizer.getTokens(sql).stream()
                .filter(it -> it.getTokenType() != TokenType.WHITESPACE)
                .toList();

        int position = 0;
        return parseSelect(tokens, position); // based on the first token we need to decide which method to call here for parsing
    }

    private static ASTNode parseSelect(List<Token> tokens, int position) {
        ASTNode selectNode = new ASTNode("SELECT_STATEMENT", "");
        expect("SELECT", tokens, position);
        position ++;

        // parse columns
        ASTNode columnsNode = new ASTNode("COLUMNS", "");
        selectNode.addChild(columnsNode);
        do {
            ASTNode columnNode = new ASTNode("COLUMN", tokens.get(position).getValue());
            columnsNode.addChild(columnNode);
            position ++;

            if (position < tokens.size() && tokens.get(position).getTokenType() == TokenType.COMMA) {
                position ++;
            } else {
                break;
            }
        } while (position < tokens.size());

        expect("FROM", tokens, position);
        position ++;
        ASTNode tableNode = new ASTNode("TABLE", tokens.get(position).getValue());
        selectNode.addChild(tableNode);
        position ++;

        // parse where clause
        if (position < tokens.size()) {
            if (tokens.get(position).getTokenType() == KEYWORD) {
                if (tokens.get(position).getValue().equalsIgnoreCase("WHERE") ) {
                    ASTNode whereNode = parseWhere(tokens, position);
                    selectNode.addChild(whereNode);
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

        return selectNode;
    }

    private static ASTNode parseWhere(List<Token> tokens, int position) {
        ASTNode whereNode = new ASTNode("WHERE", "");
        position ++;
        ASTNode leftSide = new ASTNode("IDENTIFIER", tokens.get(position).getValue());
        whereNode.addChild(leftSide);
        position ++;
        ASTNode operator = new ASTNode("OPERATOR", tokens.get(position).getValue());
        whereNode.addChild(operator);
        position ++;

        ASTNode rightSide = new ASTNode("LITERAL", tokens.get(position).getValue());
        whereNode.addChild(rightSide);
        position ++;
        return whereNode;
    }

    private static void expect(final String keywordValue, List<Token> tokens, int position) {
        if (tokens.get(position).getTokenType() != KEYWORD || !tokens.get(position).getValue().equalsIgnoreCase(keywordValue)) {
            throw new SyntaxError("Syntax error: Expected " + KEYWORD + " '" + keywordValue + "', found " + tokens.get(position).getTokenType() + "'" + tokens.get(position).getValue() + "'");
        }
    }
}
