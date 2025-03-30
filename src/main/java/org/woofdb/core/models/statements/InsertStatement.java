package org.woofdb.core.models.statements;

import org.woofdb.core.models.StatementType;

import java.util.List;

public class InsertStatement extends Statement {
    private String table;
    private List<String> values;
    private List<String> columns;

    public InsertStatement() {
        super(StatementType.STATEMENT_INSERT);
    }

    public String getTable() {
        return table;
    }

    public void setTable(final String table) {
        this.table = table;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(final List<String> values) {
        this.values = values;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(final List<String> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "InsertStatement{" +
                "table='" + table + '\'' +
                ", values=" + values +
                ", columns=" + columns +
                '}';
    }
}
