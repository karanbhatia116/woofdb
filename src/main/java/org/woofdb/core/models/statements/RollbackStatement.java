package org.woofdb.core.models.statements;

import org.woofdb.core.models.StatementType;

public final class RollbackStatement extends Statement {
    public RollbackStatement() {
        super(StatementType.STATEMENT_ROLLBACK);
    }
}
