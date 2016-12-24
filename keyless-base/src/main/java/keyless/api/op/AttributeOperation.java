package main.java.keyless.api.op;


import main.java.keyless.api.Operation;

/**
 * Created by georg on 1/28/2016.
 */
public class AttributeOperation implements Operation {
    public AttributeOperation(Object left, Object right, Operator op) {
        this.left = left;
        this.right = right;
        this.operator = op;
    }

    public static Operation all() {
        return new AttributeOperation(1, 1, Operator.eq);
    }

    Object left;
    Object right;
    Operator operator;

    @Override
    public Operation or(Operation op) {
        return new AttributeOperation(this, op.getOperator(), Operator.or);
    }

    @Override
    public Operation and(Operation op) {
        return new AttributeOperation(this, op.getOperator(), Operator.and);
    }

    @Override
    public Operation negate() {
        return new AttributeOperation(this, null, Operator.not);
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public boolean evaluate() {
        if (left.equals(right)) return true;

        else if (left instanceof Operation && right instanceof Operation) {
            switch (operator) {
                case eq:
                    return false;
                case and:
                    return ((AttributeOperation) left).evaluate() && ((AttributeOperation) right).evaluate();
                case or:
                    return ((AttributeOperation) left).evaluate() || ((AttributeOperation) right).evaluate();
                case not:
                    return !((AttributeOperation) left).evaluate();
            }
        }
        return false;

    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AttributeOperation)) return false;
        AttributeOperation other = (AttributeOperation) obj;
        return left.equals(other.left) && right.equals(other.right) && operator.equals(other.operator);
    }
}
