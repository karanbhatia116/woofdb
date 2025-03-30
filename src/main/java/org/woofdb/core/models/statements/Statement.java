package org.woofdb.core.models.statements;

import org.woofdb.core.models.StatementType;

public abstract class Statement {
    private StatementType statementType;

    public Statement(final StatementType statementType) {
        this.statementType = statementType;
    }

    public StatementType getStatementType() {
        return statementType;
    }
}
