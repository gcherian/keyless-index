package keyless.index;

import keyless.api.Hashable;
import keyless.api.Procedure;
import keyless.api.hash.HashableFunction;
import keyless.api.hash.TObjectHash;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;

/**
 * Created by georg on 1/26/2016.
 */
public class FullUniqueIndex<T> extends TObjectHash<T> implements Index<T> {

    protected final Hashable strategy;

    public FullUniqueIndex(Hashable strategy) {
        this.strategy = strategy;
    }

    public FullUniqueIndex(Hashable strategy, int capacity) {
        this.strategy = strategy;
        setUp(capacity);
    }

    public FullUniqueIndex(Function... functions) {
        this.strategy = new HashableFunction<>(functions);
    }

    @Override
    public int put(T value) {
        int index = insertKey(value);
        postInsertHook(false);
        return index;
    }

    @Override
    public Object get(T key) {
        int index = index(key);
        if (index >= 0) return _set[index];
        return null;
    }

    @Override
    public Object getFirst() {
        for (int i = 0; i < +_set.length; i++)
            if (_set[i] != null && _set[i] != FREE && _set[i] != REMOVED) return _set[i];
        return null;
    }

    @Override
    public boolean foreach(Procedure proc) {
        return false;
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public Index transform(Function f) {
        return null;
    }


    @Override
    protected void rehash(int newCapacity) {
        Object oldVals[] = _set;
        _set = new Object[newCapacity];
        Arrays.fill(_set, FREE);

        for (int i = 0; i < oldVals.length; i++) {
            Object o = oldVals[i];
            if (o == FREE || o == REMOVED) continue;
            ;

            insertKey((T) o);
        }

    }

    protected int buildHash(Object obj) {
        return strategy.hashCode(obj);
    }
}
