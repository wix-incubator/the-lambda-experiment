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

    @Test
    public void testPerformance() {

        LambdaSignature<Function2<String, Integer, Integer>> lambdaSignature = Lambda(String.class, var(Integer.class), var(Integer.class));
        long start = System.nanoTime();
        Function2<String, Integer, Integer> f = lambdaSignature.build("Integer.toString(a+b+r)", val("r", 12));
        System.out.printf("initial Lambda creation - %,d nSec\n", System.nanoTime() - start);

        start = System.nanoTime();
        for (int i=0; i < 1000; i++) {
            f = lambdaSignature.build("Integer.toString(a+b+r)", val("r", 12));
        }
        System.out.printf("subsequent Lambda creation - %,d nSec\n", (System.nanoTime() - start)/1000);

        start = System.nanoTime();
        final int r = 12;
        Function2<String, Integer, Integer> g = new Function2<String, Integer, Integer>() {
            @Override
            public String apply(Integer a, Integer b) {
                return Integer.toString(a + b + r);
            }
        };
        System.out.printf("Anonymous class initial creation  - %,d nSec\n", System.nanoTime() - start);

        start = System.nanoTime();
        for (int i=0; i < 1000; i++) {
            g = new Function2<String, Integer, Integer>() {
                @Override
                public String apply(Integer a, Integer b) {
                    return Integer.toString(a + b + r);
                }
            };
        }
        System.out.printf("Anonymous class subsequent creation - %,d nSec\n", (System.nanoTime() - start)/1000);

        start = System.nanoTime();
        f.apply(1,2);
        System.out.printf("initial Lambda apply - %,d nSec\n", System.nanoTime() - start);

        start = System.nanoTime();
        for (int i=0; i < 1000; i++) {
            f.apply(i,i);
        }
        System.out.printf("subsequent Lambda apply - %,d nSec\n", (System.nanoTime() - start) / 1000);

        start = System.nanoTime();
        g.apply(1,2);
        System.out.printf("initial Anonymous class apply - %,d nSec\n", System.nanoTime() - start);

        start = System.nanoTime();
        for (int i=0; i < 1000; i++) {
            g.apply(i,i);
        }
        System.out.printf("subsequent Anonymous class apply - %,d nSec\n", (System.nanoTime() - start) / 1000);
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
