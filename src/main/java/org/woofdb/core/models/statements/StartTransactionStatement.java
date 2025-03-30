package org.woofdb.core.models.statements;

import org.woofdb.core.models.StatementType;

public final class StartTransactionStatement extends Statement {

    public StartTransactionStatement() {
        super(StatementType.STATEMENT_START_TRANSACTION);
    }
}
