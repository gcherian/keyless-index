package main.java.keyless.index;

import main.java.keyless.api.Hashable;
import main.java.keyless.api.hash.HashableFunction;

import java.util.function.Function;

/**
 * Created by georg on 1/28/2016.
 */
public class NonUniqueIndex extends FullUniqueIndex implements Index {
    private Hashable indexStrategy = null;

    public Hashable getIndexStrategy() {
        return indexStrategy;
    }

    public NonUniqueIndex(Hashable indexStrategy, Hashable pkStrategy) {
        super(pkStrategy);
        this.indexStrategy = indexStrategy;
    }

    public NonUniqueIndex(Function pkFunction, Function... indexFunctions) {
        super(new HashableFunction(pkFunction));
        this.indexStrategy = new HashableFunction<>(indexFunctions);
    }

    public Object get(Object key) {
        if (key instanceof FullUniqueIndex) {
            key = ((FullUniqueIndex) key).get(key);
        }
        final int hash = buildHash(key) & 0x7fffffff;
        int index = hash % _set.length;
        if (index < 0) return null;

        for (int i = 0; i < _set.length; i++) {
            int offset = index + i;
            if (_set[offset] == FREE || _set[offset] == null) return null;
            else if (_set[offset] instanceof FullUniqueIndex) {
                FullUniqueIndex fui = (FullUniqueIndex) _set[offset];
                if (indexStrategy.equals(fui.get(key), key)) return fui;
            } else if (indexStrategy.equals(_set[offset], key)) return _set[offset];
        }
        return null;
    }

    public int put(Object key) {
        consumeFreeSlot = false;
        if (key == null) return insertKeyForNull();

        final int hash = buildHash(key) & 0x7fffffff;
        int index = hash % _set.length;
        Object cur = _set[index];

        if (cur == FREE || cur == REMOVED) {
            consumeFreeSlot = true;
            _set[index] = key;

        } else if (cur instanceof FullUniqueIndex) {
            ((FullUniqueIndex) cur).put(key);

        } else if (cur != null) {
            if (indexStrategy.equals(cur, key)) {
                FullUniqueIndex fui = new FullUniqueIndex(strategy);
                fui.put(cur);
                fui.put(key);
                _set[index] = fui;
                return index;
            } else {
                index = insertKeyRehash(key, index, hash, cur);
            }
        }
        return index;

    }

    protected int buildHash(Object obj) {
        return indexStrategy.hashCode(obj);
    }


}
