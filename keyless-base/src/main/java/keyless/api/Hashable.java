package main.java.keyless.api;

/**
 * Created by georg on 1/26/2016.
 */
public interface Hashable<T> {

    public int hashCode(T obj);

    public boolean equals(T o1, T o2);
}
