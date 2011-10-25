package org.wixpress.hoopoe.lambda;

import org.junit.Test;

import static org.wixpress.hoopoe.lambda.Lambdas.Lambda;
import static org.wixpress.hoopoe.lambda.Lambdas.val;
import static org.wixpress.hoopoe.lambda.Lambdas.var;

/**
 * @author Yoav
 * @since 10/6/11
 */
public class PerformanceTest {

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

            @Override
            public Class<String> retType() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Class<?>[] varTypes() {
                return new Class<?>[0];  //To change body of implemented methods use File | Settings | File Templates.
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

                @Override
                public Class<String> retType() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public Class<?>[] varTypes() {
                    return new Class<?>[0];  //To change body of implemented methods use File | Settings | File Templates.
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

}
