package org.woofdb.core.models;

public class Statement {
    private StatementType statementType;
    private Object[] args;

    public Statement () {}

    public Statement(final StatementType statementType, final Object... args) {
        this.statementType = statementType;
        this.args = args;
    }

    public StatementType getStatementType() {
        return statementType;
    }

    public void setStatementType(final StatementType statementType) {
        this.statementType = statementType;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(final Object... args) {
        this.args = args;
    }
}
