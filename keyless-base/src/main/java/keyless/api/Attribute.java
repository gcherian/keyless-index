package keyless.api;

import java.util.function.Function;

/**
 * Created by gcherian on 1/28/2016.
 */
public interface Attribute<O, A> extends Function<O, A> {
    public Operation eq(O one, O other);

    public Operation notEq(O one, O other);

    public A valueOf(O obj);
}
