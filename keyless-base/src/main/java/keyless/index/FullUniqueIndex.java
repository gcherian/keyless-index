package main.java.keyless.index;

import main.java.keyless.api.Hashable;
import main.java.keyless.api.Procedure;
import main.java.keyless.api.hash.HashableFunction;
import main.java.keyless.api.hash.TObjectHash;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Created by georg on 1/26/2016.
 */
public class FullUniqueIndex extends TObjectHash implements Index {

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
    public int put(Object value) {
        int index = insertKey(value);
        postInsertHook(false);
        return index;
    }

    @Override
    public Object get(Object key) {
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

            insertKey(o);
        }

    }

    protected int buildHash(Object obj) {
        return strategy.hashCode(obj);
    }
}
