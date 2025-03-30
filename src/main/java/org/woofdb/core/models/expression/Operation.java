package org.woofdb.core.models.expression;

import java.util.Arrays;

public enum Operation {
    EQUALS("="),
    NOT_EQUALS("<>"),
    GT(">"),
    LT("<"),
    GTE(">="),
    LTE("<="),
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    AND("&&"),
    OR("||"),
    UNKNOWN("#UNKNOWN");


    private String literalValue;

    Operation(String literalValue) {
        this.literalValue = literalValue;
    }

    public String getLiteralValue() {
        return literalValue;
    }

    public static Operation from(final String value) {
       return Arrays.stream(Operation.values())
               .filter(it -> it.getLiteralValue().equals(value))
               .findAny()
               .orElse(Operation.UNKNOWN);
    }

    @Override
    public String toString() {
        return "Operation{" +
                "literalValue='" + literalValue + '\'' +
                '}';
    }
}
