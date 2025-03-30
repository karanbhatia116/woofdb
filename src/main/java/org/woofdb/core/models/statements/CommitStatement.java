package org.woofdb.core.models.statements;

import org.woofdb.core.models.StatementType;

public final class CommitStatement extends Statement {
    public CommitStatement() {
        super(StatementType.STATEMENT_COMMIT);
    }
}
