package keyless.api;

/**
 * Created by gcherian on 1/26/2016.
 */
public interface Procedure<T> {
    public boolean execute(Object each);
}
