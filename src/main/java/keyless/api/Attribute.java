package keyless.api;

import java.util.function.Function;

/**
 * Created by georg on 1/28/2016.
 */
public interface Attribute<O, A> extends Function<O, A> {
    public keyless.api.Operation eq(O one, O other);

    public keyless.api.Operation notEq(O one, O other);

    public A valueOf(O obj);
}
