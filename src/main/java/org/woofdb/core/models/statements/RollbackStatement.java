package org.woofdb.core.models.statements;

import org.woofdb.core.models.StatementType;

public class RollbackStatement extends Statement {
    public RollbackStatement(final StatementType statementType) {
        super(statementType);
    }
}
