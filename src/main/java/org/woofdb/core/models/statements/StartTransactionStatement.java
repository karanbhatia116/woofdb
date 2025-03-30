package org.woofdb.core.models.statements;

import org.woofdb.core.models.StatementType;

public class StartTransactionStatement extends Statement {

    public StartTransactionStatement(final StatementType statementType) {
        super(statementType);
    }
}
