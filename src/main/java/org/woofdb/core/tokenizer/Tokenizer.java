package org.woofdb.core.tokenizer;

import org.woofdb.core.models.Token;

import java.util.List;

public interface Tokenizer {
    List<Token> getTokens(final String command);
}
