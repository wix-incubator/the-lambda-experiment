package org.wixpress.hoopoe.lambda;

/**
 * @author Yoav
 * @since 10/3/11
 */
@SuppressWarnings({"MethodNameSameAsClassName"})
public class Lambdas {

    public static <T> Var<T> var(Class<T> type, String name) {
        return new Var<T>(type, name);
    }

    public static <T> Var<T> var(Class<T> type) {
        return new Var<T>(type);
    }

    public static Val val(String name, Object value) {
        return new Val(name, value);
    }

    public static Val val(Object value) {
        return new Val(value);
    }

    public static <SAM> SAMSignature<SAM> Lambda(Class<SAM> samType) {
        return new SAMSignature<SAM>(samType);
    }

    public static <SAM> SAMSignature<SAM> Lambda(Class<SAM> samType, Class<?> ... genericTypes) {
        return new SAMSignature<SAM>(samType, genericTypes);
    }

    public static <R,T> LambdaSignature<Function1<R,T>> Lambda(Class<R> retType, Var<T> var1) {
        return new LambdaSignature<Function1<R, T>>(retType, var1);
    }

    public static <R,T1, T2> LambdaSignature<Function2<R,T1, T2>> Lambda(Class<R> retType, Var<T1> var1, Var<T2> var2) {
        return new LambdaSignature<Function2<R, T1, T2>>(retType, var1, var2);
    }

    public static <R,T1, T2, T3> LambdaSignature<Function3<R,T1, T2, T3>> Lambda(Class<R> retType, Var<T1> var1, Var<T2> var2, Var<T3> var3) {
        return new LambdaSignature<Function3<R, T1, T2, T3>>(retType, var1, var2, var3);
    }
}
