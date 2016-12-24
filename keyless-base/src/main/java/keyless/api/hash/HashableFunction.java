package main.java.keyless.api.hash;

import main.java.keyless.api.Hashable;

import java.util.function.Function;

/**
 * Created by georg on 1/26/2016.
 */
public class HashableFunction<T> implements Hashable<T> {

    private final Function[] functions;

    public HashableFunction(Function... fs) {
        this.functions = fs;
    }

    @Override
    public int hashCode(T obj) {
        int hash = 0;
        for (Function f : functions) {
            if (hash == 0) hash = f.apply(obj).hashCode();
            else hash = HashFunctions.combineHashes().apply(new Integer[]{hash, f.apply(obj).hashCode()});
        }
        return hash;
    }

    @Override
    public boolean equals(T o1, T o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 == null || o2 == null) return false;
        boolean equal = true;
        for (Function f : functions)
            equal = equal & f.apply(o1).equals(f.apply(o2));
        return equal;
    }
}
