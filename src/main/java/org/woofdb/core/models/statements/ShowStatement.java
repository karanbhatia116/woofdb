package org.woofdb.core.models.statements;

import org.woofdb.core.models.ResourceType;
import org.woofdb.core.models.StatementType;

public final class ShowStatement extends Statement{
    private ResourceType resourceType;
    public ShowStatement() {
        super(StatementType.STATEMENT_SHOW);
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(final ResourceType resourceType) {
        this.resourceType = resourceType;
    }
}

