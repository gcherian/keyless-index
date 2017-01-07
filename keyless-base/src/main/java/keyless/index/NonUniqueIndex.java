package keyless.index;

import keyless.api.Hashable;
import keyless.api.hash.HashableFunction;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by gcherian on 1/28/2016.
 */
public class NonUniqueIndex<T> extends FullUniqueIndex<T> implements Index<T> {
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

    public Object get(T key) {

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

    public int put(T key) {
        int index = insertValue(key);
        postInsertHook(false);
        return index;

    }

    protected int insertValue(T key) {
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
                return insertKeyRehash(key, index, hash, cur);
            }
        }
        return index;
    }

    @Override
    public <V> Index map(Function<T, V> f) {
        Index<V> newIndex = new NonUniqueIndex<V>(strategy, indexStrategy);
        foreach(each -> {
            if (each != FREE && each != REMOVED) {
                if (each instanceof FullUniqueIndex) {
                    FullUniqueIndex<T> fui = (FullUniqueIndex) each;
                    fui.foreach(e -> {
                        if (e != FREE && e != REMOVED) {
                            newIndex.put(f.apply((T) e));
                        }
                        return true;
                    });
                } else {
                    newIndex.put(f.apply((T) each));
                }

            }
            return true;
        });
        return newIndex;
    }

    @Override
    public Index<T> filter(Predicate<T> f) {
        Index newIndex = new NonUniqueIndex<T>(strategy, indexStrategy);

        foreach(each -> {
            if (each != FREE && each != REMOVED) {
                if (each instanceof FullUniqueIndex) {
                    FullUniqueIndex<T> fui = (FullUniqueIndex) each;
                    fui.foreach(e -> {
                        if (e != FREE && e != REMOVED && f.test((T) each)) {
                            newIndex.put((T) e);
                        }
                        return true;
                    });
                } else {
                    if (f.test((T) each)) {
                        newIndex.put(each);
                    }
                }

            }
            return true;
        });


        return newIndex;
    }

    protected int buildHash(Object obj) {
        return indexStrategy.hashCode(obj);
    }

    @Override
    public Iterator<T> iterator() {
        return new MultiValueIterator<T>();
    }

    class MultiValueIterator<T> implements Iterator<T> {
        FullUniqueIndex<T> nextList = null;
        Iterator<T> nextIterator = null;

        int cursor = 0;
        int lastRet = -1;


        public boolean hasNext() {
            boolean hasValue = false;
            for (int i = cursor; i < _set.length; i++) {
                if (_set[i] != FREE && _set[i] != REMOVED) hasValue = true;
            }
            return (nextList != null && nextIterator.hasNext()) || (hasValue && cursor < _set.length);
        }


        @Override
        public T next() {
            T next = null;
            if (nextList != null && nextIterator != null) {
                if (nextIterator.hasNext())
                    next = nextIterator.next();
                else {
                    nextList = null;
                    nextIterator = null;
                }
            }

            if (next == null) {
                do {
                    next = nextInner();
                } while (hasNext() && (next == null || next == FREE || next == REMOVED));
                if (next != FREE && next != REMOVED) {
                    if (next instanceof FullUniqueIndex) {
                        if (nextList == null) {
                            nextList = (FullUniqueIndex<T>) next;
                            nextIterator = nextList.iterator();
                            next = nextIterator.next();
                        }

                    } else return next;

                } else return null;

            }
            return next;
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
