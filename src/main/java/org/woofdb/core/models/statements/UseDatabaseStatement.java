package org.woofdb.core.models.statements;

import org.woofdb.core.models.StatementType;

public final class UseDatabaseStatement extends Statement {
    private String databaseName;

    public UseDatabaseStatement() {
        super(StatementType.STATEMENT_USE);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String toString() {
        return "UseDatabaseStatement{" +
                "databaseName='" + databaseName + '\'' +
                '}';
    }
}
