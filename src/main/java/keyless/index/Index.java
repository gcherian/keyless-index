package keyless.index;

import keyless.api.Procedure;

import java.util.function.Function;

/**
 * Created by georg on 1/26/2016.
 */
public interface Index {

    public int put(Object value);

    public Object get(Object key);

    public Object getFirst();

    public boolean foreach(Procedure proc);

    public Index transform(Function f);
}
