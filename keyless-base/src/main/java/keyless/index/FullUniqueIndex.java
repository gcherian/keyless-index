package keyless.index;

import keyless.api.Hashable;
import keyless.api.Procedure;
import keyless.api.hash.HashableFunction;
import keyless.api.hash.TObjectHash;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by gcherian on 1/26/2016.
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
    public Iterator<T> iterator() {
        return new ValueIterator<T>();
    }

    @Override
    public <V> Index map(Function<T, V> f) {
        Index<V> newIndex = new FullUniqueIndex<V>(strategy);
        foreach(each -> {
            if (each != FREE && each != REMOVED) {
                newIndex.put(f.apply((T) each));
            }
            return true;
        });
        return newIndex;
    }

    @Override
    public Index<T> filter(Predicate<T> f) {
        Index newIndex = new FullUniqueIndex(strategy);

        foreach(each -> {
            if (each != FREE && each != REMOVED && f.test((T) each)) {
                newIndex.put(each);
            }
            return true;
        });


        return newIndex;
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

    protected class ValueIterator<T> implements Iterator<T> {


        int cursor = 0;
        int lastRet = -1;


        public boolean hasNext() {
            boolean hasValue = false;
            for (int i = cursor; i < _set.length; i++) {
                if (_set[i] != FREE && _set[i] != REMOVED) hasValue = true;
            }
            return hasValue && cursor < _set.length;
        }

        public T next() {
            T next = null;
            do {
                next = nextInner();
            } while (hasNext() && (next == null || next == FREE || next == REMOVED));
            if (next != FREE && next != REMOVED) return next;
            else return null;
        }

        protected T nextInner() {
            try {
                int i = cursor;
                T next = (T) _set[i];
                lastRet = i;
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new RuntimeException("Index cannot be modified using iterators.");
        }


    }
}


