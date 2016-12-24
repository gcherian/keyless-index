package main.java.keyless.api;

import main.java.keyless.api.op.Operator;

/**
 * Created by georg on 1/28/2016.
 */
public interface Operation {
    public Operation or(Operation op);

    public Operation and(Operation op);

    public Operation negate();

    public Operator getOperator();

    public boolean evaluate();

}
