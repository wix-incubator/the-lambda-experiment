package org.wixpress.hoopoe.lambda;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yoav
 * @since 10/3/11
 */
public class LambdaSignature<F> {

    RetType retType;
    Var<?>[] vars;
    private char nextDefaultName = 'a';
    private static final Map<LambdaClassKey, LambdaClassGenerator> classCache = new HashMap<LambdaClassKey, LambdaClassGenerator>();

    public LambdaSignature(Class<?> retType, Var<?>... vars) {
        this.vars = vars;
        this.retType = new RetType(retType);
        for (Var var: vars)
            var.setDefaultName(nextDefaultName++);
    }

    public F build(String code, Val ... vals) {
        char nextBindDefaultName = nextDefaultName;
        for (Val val: vals) {
            val.setDefaultName(nextBindDefaultName++);
        }
        LambdaClassGenerator<F> lambdaGen = generateClass(code, vals);
        try {
            Object[] valValues = new Object[vals.length];
            for (int i=0; i < vals.length; i++) {
                Val val = vals[i];
                valValues[i] = val.getValue();
            }
            return lambdaGen.lambdaConstructor.newInstance(valValues);
        } catch (InstantiationException e) {
            throw new LambdaException("failed creating Lambda instance - \n" + lambdaGen.toString(), e);
        } catch (IllegalAccessException e) {
            throw new LambdaException("failed creating Lambda instance - \n" + lambdaGen.toString(), e);
        } catch (InvocationTargetException e) {
            throw new LambdaException("failed creating Lambda instance - \n" + lambdaGen.toString(), e);
        }
    }

    private LambdaClassGenerator<F> generateClass(String code, Val[] vals) {
        LambdaClassKey key = new LambdaClassKey(retType, vars, vals, code);
        if (!classCache.containsKey(key)) {
            synchronized (classCache) {
                if (!classCache.containsKey(key)) {
                    LambdaClassGenerator<F> lambdaGen = new LambdaClassGenerator<F>(this);
                    lambdaGen.generateClass(code, vals, key);
                    classCache.put(key, lambdaGen);
                    return lambdaGen;
                }
            }
        }
        //noinspection unchecked
        return classCache.get(key);
    }

}
