package org.woofdb.core.models.statements;

import org.woofdb.core.models.StatementType;

sealed public abstract class Statement
        permits CommitStatement, CreateStatement, DropStatement, InsertStatement, RollbackStatement, SelectStatement, StartTransactionStatement, UpdateStatement, UseDatabaseStatement {
    private StatementType statementType;

    public Statement(final StatementType statementType) {
        this.statementType = statementType;
    }

    public StatementType getStatementType() {
        return statementType;
    }
}
