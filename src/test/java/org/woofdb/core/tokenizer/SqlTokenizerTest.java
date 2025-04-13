package org.woofdb.core.tokenizer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.woofdb.core.exceptions.SyntaxError;
import org.woofdb.core.models.Token;
import org.woofdb.core.models.TokenType;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SqlTokenizerTest {

    private final SqlTokenizer tokenizer = new SqlTokenizer();

    @Test
    void isKeywordShouldReturnTrueForSupportedKeywords() {
        // Test a few keywords
        assertTrue(SqlTokenizer.isKeyword("SELECT"));
        assertTrue(SqlTokenizer.isKeyword("FROM"));
        assertTrue(SqlTokenizer.isKeyword("WHERE"));

        // Test case insensitivity
        assertTrue(SqlTokenizer.isKeyword("select"));
        assertTrue(SqlTokenizer.isKeyword("Select"));
        assertTrue(SqlTokenizer.isKeyword("INSERT"));
        assertTrue(SqlTokenizer.isKeyword("insert"));
    }

    @Test
    void isKeywordShouldReturnFalseForNonKeywords() {
        assertFalse(SqlTokenizer.isKeyword("NOTKEYWORD"));
        assertFalse(SqlTokenizer.isKeyword("HELLO"));
        assertFalse(SqlTokenizer.isKeyword("123"));
        assertFalse(SqlTokenizer.isKeyword(""));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void getTokensShouldReturnEmptyListForNullOrBlankInput(String input) {
        List<Token> tokens = tokenizer.getTokens(input);
        assertTrue(tokens.isEmpty());
    }

    @Test
    void getTokensShouldHandleWhitespace() {
        List<Token> tokens = tokenizer.getTokens("SELECT \t\n * \r\n FROM users \n;");
        assertEquals(9, tokens.size());
    }

    @Test
    void getTokensShouldIdentifyKeywords() {
        List<Token> tokens = tokenizer.getTokens("SELECT FROM WHERE");
        assertEquals(5, tokens.size());

        assertEquals(TokenType.KEYWORD, tokens.get(0).getTokenType());
        assertEquals("SELECT", tokens.get(0).getValue());

        assertEquals(TokenType.WHITESPACE, tokens.get(1).getTokenType());

        assertEquals(TokenType.KEYWORD, tokens.get(2).getTokenType());
        assertEquals("FROM", tokens.get(2).getValue());

        assertEquals(TokenType.WHITESPACE, tokens.get(3).getTokenType());

        assertEquals(TokenType.KEYWORD, tokens.get(4).getTokenType());
        assertEquals("WHERE", tokens.get(4).getValue());
    }

    @Test
    void getTokensShouldIdentifyMixedCaseKeywords() {
        List<Token> tokens = tokenizer.getTokens("Select From where");

        assertEquals(TokenType.KEYWORD, tokens.get(0).getTokenType());
        assertEquals("Select", tokens.get(0).getValue());

        assertEquals(TokenType.KEYWORD, tokens.get(2).getTokenType());
        assertEquals("From", tokens.get(2).getValue());

        assertEquals(TokenType.KEYWORD, tokens.get(4).getTokenType());
        assertEquals("where", tokens.get(4).getValue());
    }

    @Test
    void getTokensShouldIdentifyIdentifiers() {
        List<Token> tokens = tokenizer.getTokens("table_name column1 my_table2");

        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getTokenType());
        assertEquals("table_name", tokens.get(0).getValue());

        assertEquals(TokenType.IDENTIFIER, tokens.get(2).getTokenType());
        assertEquals("column1", tokens.get(2).getValue());

        assertEquals(TokenType.IDENTIFIER, tokens.get(4).getTokenType());
        assertEquals("my_table2", tokens.get(4).getValue());
    }

    @Test
    void getTokensShouldIdentifyAsteriskAsIdentifier() {
        List<Token> tokens = tokenizer.getTokens("*");

        assertEquals(1, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getTokenType());
        assertEquals("*", tokens.get(0).getValue());
    }

    @Test
    void getTokensShouldHandleComments() {
        List<Token> tokens = tokenizer.getTokens("SELECT * -- this is a comment");

        assertEquals(5, tokens.size()); // also includes COMMENT as a token type
        assertEquals(TokenType.KEYWORD, tokens.get(0).getTokenType());
        assertEquals("SELECT", tokens.get(0).getValue());

        assertEquals(TokenType.WHITESPACE, tokens.get(1).getTokenType());

        assertEquals(TokenType.IDENTIFIER, tokens.get(2).getTokenType());
        assertEquals("*", tokens.get(2).getValue());

        assertEquals(TokenType.WHITESPACE, tokens.get(3).getTokenType());

        assertEquals(TokenType.COMMENT, tokens.get(4).getTokenType());
        assertEquals("-- this is a comment", tokens.get(4).getValue());
    }

    @Test
    void getTokensShouldHandleStringLiterals() {
        List<Token> tokens = tokenizer.getTokens("'hello world'");

        assertEquals(1, tokens.size());
        assertEquals(TokenType.LITERAL, tokens.get(0).getTokenType());
        assertEquals("'hello world'", tokens.get(0).getValue());
    }

    @Test
    void getTokensShouldHandleNumericLiterals() {
        List<Token> tokens = tokenizer.getTokens("123 45.67");

        assertEquals(3, tokens.size());
        assertEquals(TokenType.LITERAL, tokens.get(0).getTokenType());
        assertEquals("123", tokens.get(0).getValue());

        assertEquals(TokenType.WHITESPACE, tokens.get(1).getTokenType());

        assertEquals(TokenType.LITERAL, tokens.get(2).getTokenType());
        assertEquals("45.67", tokens.get(2).getValue());
    }

    @Test
    void getTokensShouldHandleOperators() {
        List<Token> tokens = tokenizer.getTokens("= > <");

        assertEquals(5, tokens.size());
        assertEquals(TokenType.OPERATOR, tokens.get(0).getTokenType());
        assertEquals("=", tokens.get(0).getValue());

        assertEquals(TokenType.WHITESPACE, tokens.get(1).getTokenType());

        assertEquals(TokenType.OPERATOR, tokens.get(2).getTokenType());
        assertEquals(">", tokens.get(2).getValue());

        assertEquals(TokenType.WHITESPACE, tokens.get(3).getTokenType());

        assertEquals(TokenType.OPERATOR, tokens.get(4).getTokenType());
        assertEquals("<", tokens.get(4).getValue());
    }

    @Test
    void getTokensShouldHandleComma() {
        List<Token> tokens = tokenizer.getTokens(",");

        assertEquals(1, tokens.size());
        assertEquals(TokenType.COMMA, tokens.get(0).getTokenType());
        assertEquals(",", tokens.get(0).getValue());
    }

    @Test
    void getTokensShouldHandleSemicolon() {
        List<Token> tokens = tokenizer.getTokens(";");

        assertEquals(1, tokens.size());
        assertEquals(TokenType.SEMICOLON, tokens.get(0).getTokenType());
        assertEquals(";", tokens.get(0).getValue());
    }

    @Test
    void getTokensShouldHandleParentheses() {
        List<Token> tokens = tokenizer.getTokens("()");
        // Parentheses are skipped according to the implementation
        assertEquals(0, tokens.size());
    }

    @Test
    void getTokensShouldThrowSyntaxErrorForInvalidCharacters() {
        assertThrows(SyntaxError.class, () -> tokenizer.getTokens("SELECT * @"));
    }

    @Test
    void getTokensShouldHandleComplexQuery() {
        String sql = "SELECT id, name FROM users WHERE age > 18 AND status = 'active';";
        List<Token> tokens = tokenizer.getTokens(sql);

        // Verify the number of tokens
        assertEquals(27, tokens.size());

        // Verify a few key tokens
        assertEquals(TokenType.KEYWORD, tokens.get(0).getTokenType());
        assertEquals("SELECT", tokens.get(0).getValue());

        assertEquals(TokenType.IDENTIFIER, tokens.get(2).getTokenType());
        assertEquals("id", tokens.get(2).getValue());

        assertEquals(TokenType.COMMA, tokens.get(3).getTokenType());

        assertEquals(TokenType.IDENTIFIER, tokens.get(5).getTokenType());
        assertEquals("name", tokens.get(5).getValue());

        assertEquals(TokenType.KEYWORD, tokens.get(7).getTokenType());
        assertEquals("FROM", tokens.get(7).getValue());

        assertEquals(TokenType.IDENTIFIER, tokens.get(9).getTokenType());
        assertEquals("users", tokens.get(9).getValue());

        assertEquals(TokenType.KEYWORD, tokens.get(11).getTokenType());
        assertEquals("WHERE", tokens.get(11).getValue());
    }

    @ParameterizedTest
    @MethodSource("provideSqlQueries")
    void getTokensShouldHandleVariousQueries(String sql, int expectedTokenCount) {
        List<Token> tokens = tokenizer.getTokens(sql);
        assertEquals(expectedTokenCount, tokens.size());
    }

    private static Stream<Arguments> provideSqlQueries() {
        return Stream.of(
                Arguments.of("CREATE TABLE users (id INT, name VARCHAR);", 15),
                Arguments.of("INSERT INTO users VALUES (1, 'John');", 13),
                Arguments.of("UPDATE users SET name = 'Jane' WHERE id = 1;", 20),
                Arguments.of("DROP TABLE users;", 6),
                Arguments.of("SELECT * FROM users LIMIT 10;", 12)
        );
    }

    @Test
    void getTokensShouldHandleUnterminatedStringLiteral() {
        String sql = "SELECT * FROM users WHERE name = 'unterminated";
        List<Token> tokens = tokenizer.getTokens(sql);

        // The last token should be the unterminated string
        Token lastToken = tokens.get(tokens.size() - 1);
        assertEquals(TokenType.LITERAL, lastToken.getTokenType());
        assertEquals("'unterminated", lastToken.getValue());
    }
}