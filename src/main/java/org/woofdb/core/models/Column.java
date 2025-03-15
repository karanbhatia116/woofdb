package org.woofdb.core.models;

public class Column {
    private String name;
    private DataType dataType;
    private boolean nullable;

    public Column() {
    }

    public Column(final String name, final DataType dataType, final boolean nullable) {
        this.name = name;
        this.dataType = dataType;
        this.nullable = nullable;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(final DataType dataType) {
        this.dataType = dataType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(final boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public String toString() {
        return "Column{" +
                "name='" + name + '\'' +
                ", dataType=" + dataType +
                ", nullable=" + nullable +
                '}';
    }
}
