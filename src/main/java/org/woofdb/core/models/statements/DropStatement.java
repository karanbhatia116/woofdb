package org.woofdb.core.models.statements;


import org.woofdb.core.models.ResourceType;
import org.woofdb.core.models.StatementType;

public final class DropStatement extends Statement {
    private ResourceType resourceType;
    private String resourceName;

    public DropStatement() {
        super(StatementType.STATEMENT_DROP);
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(final ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(final String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public String toString() {
        return "DropStatement{" +
                "resourceType=" + resourceType +
                ", resourceName='" + resourceName + '\'' +
                '}';
    }
}
