package org.woofdb.core.models.statements;

import org.woofdb.core.models.ResourceType;

public class CreateDatabaseStatement extends CreateStatement {

    public CreateDatabaseStatement() {
        super(ResourceType.DATABASE);
    }

    private String databaseName;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String toString() {
        return "CreateDatabaseStatement{" +
                "databaseName='" + databaseName + '\'' +
                '}';
    }
}
