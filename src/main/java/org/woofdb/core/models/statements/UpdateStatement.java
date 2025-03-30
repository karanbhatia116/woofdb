package org.woofdb.core.models.statements;

import org.woofdb.core.models.StatementType;
import org.woofdb.core.models.expression.Expression;

import java.util.Map;

public final class UpdateStatement extends Statement {

    public UpdateStatement() {
        super(StatementType.STATEMENT_UPDATE);
    }

    private String tableName;
    private Map<String, String> updates;
    private Expression condition;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    public Map<String, String> getUpdates() {
        return updates;
    }

    public void setUpdates(final Map<String, String> updates) {
        this.updates = updates;
    }

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(final Expression condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "UpdateStatement{" +
                "tableName='" + tableName + '\'' +
                ", updates=" + updates +
                ", condition=" + condition +
                '}';
    }
}
