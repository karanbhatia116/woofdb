package org.woofdb.core.models.statements;


import org.woofdb.core.models.ResourceType;
import org.woofdb.core.models.StatementType;

public sealed class CreateStatement extends Statement permits CreateDatabaseStatement, CreateTableStatement {
    private ResourceType resourceType;

    public ResourceType getResourceType() {
        return resourceType;
    }

    public CreateStatement(final ResourceType resourceType) {
        super(StatementType.STATEMENT_CREATE);
        this.resourceType = resourceType;
    }

    @Override
    public String toString() {
        return "CreateStatement{" +
                "resourceType=" + resourceType +
                '}';
    }
}
