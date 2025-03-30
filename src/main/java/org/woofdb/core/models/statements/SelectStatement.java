package org.woofdb.core.models.statements;

import org.woofdb.core.models.StatementType;
import org.woofdb.core.models.expression.BinaryExpression;

import java.util.List;

public final class SelectStatement extends Statement {

    private List<String> columns;
    private String from;
    private BinaryExpression where;

    public SelectStatement() {
        super(StatementType.STATEMENT_SELECT);
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(final List<String> columns) {
        this.columns = columns;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(final String from) {
        this.from = from;
    }

    public BinaryExpression getWhere() {
        return where;
    }

    public void setWhere(final BinaryExpression where) {
        this.where = where;
    }

    @Override
    public String toString() {
        return "SelectStatement{" +
                "columns=" + columns +
                ", from='" + from + '\'' +
                ", where=" + where +
                '}';
    }
}
