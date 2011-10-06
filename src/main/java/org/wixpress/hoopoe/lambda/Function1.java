package org.wixpress.hoopoe.lambda;

/**
* @author Yoav
* @since 10/3/11
*/
public interface Function1<R, T> {
    public R apply(T t);
}
