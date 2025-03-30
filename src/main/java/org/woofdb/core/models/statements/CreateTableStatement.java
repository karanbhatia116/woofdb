package org.woofdb.core.models.statements;

import org.woofdb.core.models.Column;
import org.woofdb.core.models.ResourceType;

import java.util.List;

public final class CreateTableStatement extends CreateStatement {

    public CreateTableStatement() {
        super(ResourceType.TABLE);
    }

    private List<Column> columns;
    private String tableName;

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(final List<Column> columns) {
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return "CreateTableStatement{" +
                "columns=" + columns +
                ", tableName='" + tableName + '\'' +
                '}';
    }
}
