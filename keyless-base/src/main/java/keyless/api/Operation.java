package keyless.api;

import keyless.api.op.Operator;

/**
 * Created by gcherian on 1/28/2016.
 */
public interface Operation {
    public Operation or(Operation op);

    public Operation and(Operation op);

    public Operation negate();

    public Operator getOperator();

    public boolean evaluate();

}
