package org.wixpress.hoopoe.lambda;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.wixpress.hoopoe.lambda.Lambdas.*;

/**
 * @author Yoav
 * @since 10/3/11
 */

public class TestFunctionalCollection {

    static class FList<T> extends ArrayList<T> {

        private Class<T> itemsType;

        public FList(Class<T> itemsType) {
            this.itemsType = itemsType;
        }

        public <R> FList<R> map(Function1<R, T> mapper) {
            FList<R> newList = new FList<R>(mapper.retType());
            for (T t: this)
                newList.add(mapper.apply(t));
            return newList;
        }

        public <R> FList<R> map(Class<R> retType, String code, Val ... vals) {
            Function1<R, T> mapper = Lambda(retType, var(itemsType)).build(code, vals);
            return map(mapper);
        }

        public <R> PreparedMapper<R, T> mapTo(Class<R> retType) {
            return new PreparedMapper<R, T>(this, retType);
        }

    }

    public static class PreparedMapper<R, T> {
        private FList<T> fList;
        private Class<R> retType;

        public PreparedMapper(FList<T> fList, Class<R> retType) {
            this.fList = fList;
            this.retType = retType;
        }

        public FList<R> with(String code, Val ... vals) {
            return fList.map(Lambda(retType, var(fList.itemsType)).build(code, vals));
        }
    }

    private <V> FList<V> buildList(Class<V> itemType, V ... values) {
        FList<V> aList = new FList<V>(itemType);
        aList.addAll(Arrays.asList(values));
        return aList;
    }

    @Test
    public void testMapOnListOption1() {
        FList<Integer> aList = buildList(Integer.class,1,2,3);
        FList<Integer> bList = aList.map(Integer.class, "a*a");
        assertThat(bList, hasItems(1, 4, 9));
    }

    @Test
    public void testMapOnListOption2() {
        FList<Integer> aList = buildList(Integer.class,1,2,3);
        FList<Integer> bList = aList.map(Lambda(Integer.class, var(Integer.class)).build("a*a"));
        assertThat(bList, hasItems(1, 4, 9));
    }

    @Test
    public void testMapOnListOption3() {
        FList<Integer> aList = buildList(Integer.class,1,2,3);
        FList<Integer> bList = aList.mapTo(Integer.class).with("a*a");
        assertThat(bList, hasItems(1, 4, 9));
    }

    @Test
    public void testMapOnListWithVal() {
        FList<Integer> aList = buildList(Integer.class,1,2,3);
        FList<Integer> bList = aList.mapTo(Integer.class).with("a*a+b", val(12));
        assertThat(bList, hasItems(13, 16, 21));
    }

    @Test
    public void testMapOnListToStrings() {
        FList<Integer> aList = buildList(Integer.class,1,2,3);
        FList<String> bList = aList.map(String.class, "Integer.toString(a)");
        assertThat(bList, hasItems("1", "2", "3"));
    }

}
