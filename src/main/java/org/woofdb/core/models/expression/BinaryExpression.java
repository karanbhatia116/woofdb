package org.woofdb.core.models.expression;

public class BinaryExpression extends Expression {
    private Expression leftSide;
    private Operation operation;
    private Expression rightSide;

    public Expression getLeftSide() {
        return leftSide;
    }

    public void setLeftSide(final Expression leftSide) {
        this.leftSide = leftSide;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(final Operation operation) {
        this.operation = operation;
    }

    public Expression getRightSide() {
        return rightSide;
    }

    public void setRightSide(final Expression rightSide) {
        this.rightSide = rightSide;
    }

    @Override
    public String toString() {
        return "BinaryExpression{" +
                "leftSide=" + leftSide +
                ", operation=" + operation +
                ", rightSide=" + rightSide +
                '}';
    }
}
