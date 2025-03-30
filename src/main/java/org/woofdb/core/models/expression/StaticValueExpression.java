package org.woofdb.core.models.expression;

public class StaticValueExpression extends Expression {
    private Object value;

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    public StaticValueExpression withValue(final Object value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return "StaticValueExpression{" +
                "value=" + value +
                '}';
    }
}
