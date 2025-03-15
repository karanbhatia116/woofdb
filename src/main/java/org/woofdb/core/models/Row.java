package org.woofdb.core.models;

import java.util.Arrays;

public class Row {
    private Object[] values;

    public Row(final int columnCount) {
        this.values = new Object[columnCount];
    }

    public void setValue(int columnIndex, Object value) {
        this.values[columnIndex] = value;
    }

    public Object getValue(int columnIndex)  {
        return this.values[columnIndex];
    }

    public Object[] getValues() {
        return this.values;
    }

    public void setValues(final Object[] values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "Row{" +
                "values=" + Arrays.toString(values) +
                '}';
    }
}
