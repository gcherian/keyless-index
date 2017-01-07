package keyless.api.op;


import keyless.api.Attribute;
import keyless.api.Operation;

/**
 * Created by gcherian on 1/28/2016.
 */
public class TupleAttribute<O, A> implements Attribute<O, A> {

    public TupleAttribute(Attribute[] attributes) {
        this.attributes = attributes;
    }

    Attribute[] attributes;

    @Override
    public Operation eq(O one, O other) {
        Operation op = AttributeOperation.all();
        for (Attribute attribute : attributes) {
            op = op.and(attribute.eq(one, other));
        }
        return op;
    }

    @Override
    public Operation notEq(O one, O other) {
        return eq(one, other).negate();
    }

    @Override
    public A valueOf(O obj) {
        return (A) new TupleAttribute<>((Attribute[]) obj);
    }

    @Override
    public A apply(O o) {
        return valueOf(o);
    }
}
