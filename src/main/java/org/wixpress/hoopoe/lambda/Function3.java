package org.wixpress.hoopoe.lambda;

/**
* @author Yoav
* @since 10/3/11
*/
public interface Function3<R, T1, T2, T3> {
    public R apply(T1 t1, T2 t2, T3 t3);
}
