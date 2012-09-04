import fj.F;
import fj.data.Array;
import org.junit.Test;

import static fj.data.Array.array;
import static fj.data.List.fromString;
import static fj.function.Characters.isLowerCase;
import static fj.Show.arrayShow;
import static fj.Show.intShow;
import static org.wixpress.hoopoe.lambda.Lambdas.*;

/**
 * @author Yoav
 * @since 10/25/11
 */
public class FunctionalJavaExamples {

    interface FF<A, B> {
        public B f(A a);
    }

    class FImpl<A, B> extends F<A, B> {

        private FF<A, B> ff;

        FImpl(FF<A, B> ff) {
            this.ff = ff;
        }

        @Override
        public B f(A a) {
            return ff.f(a);
        }
    }

    private <A, B> F<A, B> F(Class<A> ofType, Class<B> returnType, String code) {
        return new FImpl<A, B>(Lambda(FF.class, ofType, returnType).build(code));
    }


    @Test
    public void arrayExists() {
        final Array<String> a = array("Hello", "There", "what", "DAY", "iS", "iT");
        final boolean b = a.exists(new F<String, Boolean>() {
            public Boolean f(final String s) {
                return fromString(s).forall(isLowerCase);
            }
        });
        System.out.println(b);
    }

    @Test
    public void arrayExists_Lambda() {
        final Array<String> a = array("Hello", "There", "what", "DAY", "iS", "iT");
        final boolean b = a.exists(F(String.class, Boolean.class, "fj.data.List.fromString(a).forall(fj.function.Characters.isLowerCase)"));
        System.out.println(b);
    }

    @Test
    public void arrayBind() {
        final Array<Integer> a = array(97, 44, 67, 3, 22, 90, 1, 77, 98, 1078, 6, 64, 6, 79, 42);
        final Array<Integer> b = a.bind(new F<Integer, Array<Integer>>() {
            public Array<Integer> f(final Integer i) {
                return array(500, i);
            }
        });
        arrayShow(intShow).println(b);
        // {500,97,500,44,500,67,500,3,500,22,500,90,500,1,500,77,500,98,500,1078,500,6,500,64,500,6,500,79,500,42}
    }

    @Test
    public void arrayBind_Lambda() {
        final Array<Integer> a = array(97, 44, 67, 3, 22, 90, 1, 77, 98, 1078, 6, 64, 6, 79, 42);
        F<Integer, Array<Integer>> f = (F)F(Integer.class, Array.class, "fj.data.Array.array(new Integer[] {500, a})");
        final Array<Integer> b = a.bind(f);
        arrayShow(intShow).println(b);
    }

}
