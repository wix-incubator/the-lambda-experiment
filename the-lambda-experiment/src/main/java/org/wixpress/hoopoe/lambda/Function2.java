package org.wixpress.hoopoe.lambda;

/**
* @author Yoav
* @since 10/3/11
*/
public interface Function2<R, T1, T2> extends SelfDescribingFunction<R>{
    public R apply(T1 t1, T2 t2);
}
