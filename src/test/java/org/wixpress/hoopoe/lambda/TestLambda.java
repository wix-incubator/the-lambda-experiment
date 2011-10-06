package org.wixpress.hoopoe.lambda;

import org.junit.Test;

import java.util.*;

import static org.wixpress.hoopoe.lambda.Lambdas.Lambda;
import static org.wixpress.hoopoe.lambda.Lambdas.val;
import static org.wixpress.hoopoe.lambda.Lambdas.var;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yoav
 * @since 10/3/11
 */

public class TestLambda {

    @Test
    public void testLambda() {
        Function2<Integer, Integer, Integer> f = Lambda(Integer.class, var(Integer.class, "x"), var(Integer.class, "y")).build("x+y+z", val("z", 12));
        assertThat(f.apply(1, 2), is(15));
    }

    @Test
    public void testLambdaDefNames() {
        Function2<Integer, Integer, Integer> f = Lambda(Integer.class, var(Integer.class), var(Integer.class)).build("a+b+c", val(12));
        assertThat(f.apply(1, 2), is(15));
    }

    @Test
    public void testLambdaReuseNames() {

        LambdaSignature<Function2<Integer, Integer, Integer>> lambdaSignature = Lambda(Integer.class, var(Integer.class), var(Integer.class));

        Function2<Integer, Integer, Integer> f = lambdaSignature.build("a+b+r", val("r", 12));
        assertThat(f.apply(1, 2), is(15));
        Function2<Integer, Integer, Integer> f2 = lambdaSignature.build("a+b+c", val(15));
        assertThat(f2.apply(1, 2), is(18));
        Function2<Integer, Integer, Integer> f3 = lambdaSignature.build("a+b");
        assertThat(f3.apply(1, 2), is(3));

    }

    private <V> FunctionalList<V> buildList(Class<V> itemType, V ... values) {
        FunctionalList<V> aList = new FunctionalList<V>(itemType);
        aList.addAll(Arrays.asList(values));
        return aList;
    }

    @Test
    public void testMapOnList() {
        FunctionalList<Integer> aList = buildList(Integer.class,1,2,3);
        FunctionalList<Integer> bList = aList.map(Integer.class, "a*a");
        assertThat(bList, hasItems(1, 4, 9));
    }

    @Test
    public void testMapOnListToStrings() {
        FunctionalList<Integer> aList = buildList(Integer.class,1,2,3);
        FunctionalList<String> bList = aList.map(String.class, "Integer.toString(a)");
        assertThat(bList, hasItems("1", "2", "3"));
    }

    @Test
    public void testMapOnListWithMapTo() {
        FunctionalList<Integer> aList = buildList(Integer.class,1,2,3);
        FunctionalList<Integer> bList = aList.mapTo(Integer.class).with("a*a+b", val(12));
        assertThat(bList, hasItems(13, 16, 21));
    }

    static class FunctionalList<T> extends ArrayList<T> {

        private Class<T> itemsType;

        public FunctionalList(Class<T> itemsType) {
            this.itemsType = itemsType;
        }

        public <R> FunctionalList<R> map(Class<R> retType, Function1<R, T> mapper) {
            FunctionalList<R> newList = new FunctionalList<R>(retType);
            for (T t: this)
                newList.add(mapper.apply(t));
            return newList;
        }

        public <R> FunctionalList<R> map(Class<R> retType, String code) {
            Function1<R, T> mapper = Lambda(retType, var(itemsType)).build(code);
            return map(retType, mapper);
        }

        public <R> PreparedMapper<R, T> mapTo(Class<R> retType) {
            return new PreparedMapper<R, T>(this, retType);
        }

    }

    public static class PreparedMapper<R, T> {
        private FunctionalList<T> functionalList;
        private Class<R> retType;

        public PreparedMapper(FunctionalList<T> functionalList, Class<R> retType) {
            this.functionalList = functionalList;
            this.retType = retType;
        }

        public FunctionalList<R> with(String code, Val ... vals) {
            return functionalList.map(retType, Lambda(retType, var(functionalList.itemsType)).build(code, vals));
        }
    }

}
