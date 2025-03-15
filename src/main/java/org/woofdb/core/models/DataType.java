package org.woofdb.core.models;

public enum DataType {
    INT,
    DOUBLE,
    FLOAT,
    VARCHAR;

    DataType() {}

    DataType(int scale, int precision) {
        this.scale = scale;
        this.precision = precision;
    }

    DataType(int length) {
        this.length = length;
    }

    private Integer scale;
    private Integer precision;
    private Integer length;

    public Integer getScale() {
        return scale;
    }

    public Integer getPrecision() {
        return precision;
    }

    public Integer getLength() {
        return length;
    }
}
