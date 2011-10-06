package org.wixpress.hoopoe.lambda;

import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Yoav
 * @since 10/3/11
 */
public class LambdaSignature<F> {

    RetType retType;
    Var<?>[] vars;
    private char nextDefaultName = 'a';


    public LambdaSignature(Class<?> retType, Var<?>... vars) {
        this(new RetType(retType), vars);
        for (Var var: vars)
            var.setDefaultName(nextDefaultName++);
    }

    private LambdaSignature(RetType retType, Var<?>[] vars) {
        this.vars = vars;
        this.retType = retType;
    }

    private Val[] concat(Val[] a, Val b) {
        Val[] c = new Val[a.length+1];
        System.arraycopy(a, 0, c, 0, a.length);
        c[c.length-1] = b;
        return c;
    }

    public F build(String code, Val ... vals) {
        char nextBindDefaultName = nextDefaultName;
        for (Val val: vals) {
            val.setDefaultName(nextBindDefaultName++);
        }
        LambdaCodeGenerator lambdaGen = generateClass(code, vals);
        try {
            Class<?>[] valTypes = new Class<?>[vals.length];
            Object[] valValues = new Object[vals.length];
            for (int i=0; i < vals.length; i++) {
                Val val = vals[i];
                valTypes[i] = val.boxedType();
                valValues[i] = val.getValue();
            }

//            return lambdaGen.lambdaClass.newInstance();
            Constructor<F> constructor = lambdaGen.lambdaClass.getConstructor(valTypes);
            return constructor.newInstance(valValues);

        } catch (InstantiationException e) {
            throw new LambdaException("failed creating Lambda instance - \n" + lambdaGen.toString(), e);
        } catch (IllegalAccessException e) {
            throw new LambdaException("failed creating Lambda instance - \n" + lambdaGen.toString(), e);
        } catch (NoSuchMethodException e) {
            throw new LambdaException("failed creating Lambda instance - \n" + lambdaGen.toString(), e);
        } catch (InvocationTargetException e) {
            throw new LambdaException("failed creating Lambda instance - \n" + lambdaGen.toString(), e);
        }
    }

    private LambdaCodeGenerator generateClass(String code, Val[] vals) {
        LambdaCodeGenerator<F> lambdaGen = new LambdaCodeGenerator<F>(this);
        lambdaGen.generateClassCode(code, vals);
        try {
            ClassPool pool = ClassPool.getDefault();
            pool.insertClassPath(new ClassClassPath(this.getClass()));
            CtClass ctClass = pool.makeClass(lambdaGen.lambdaName);
            ctClass.addInterface(toCtClass(lambdaGen.functionInterface));

            for (String fieldCode: lambdaGen.fieldsCode)
                ctClass.addField(CtField.make(fieldCode, ctClass));

            ctClass.addMethod(CtNewMethod.make(
                    lambdaGen.invokeInternalCode,
                    ctClass));

            ctClass.addMethod(CtNewMethod.make(
                    lambdaGen.invokeCode,
                    ctClass));

            if (lambdaGen.constructorCode != null)
                ctClass.addConstructor(CtNewConstructor.make(lambdaGen.constructorCode, ctClass));

            //noinspection unchecked
            lambdaGen.lambdaClass = (Class<F>)ctClass.toClass();
            return lambdaGen;
        } catch (CannotCompileException e) {
            throw new LambdaException("failed creating Lambda - \n" + lambdaGen.toString(), e);
        }
    }

    private CtClass toCtClass(final Class inputClass) {
        try {
            ClassPool pool = ClassPool.getDefault();
            return pool.get(toClassName(inputClass));
        } catch (NotFoundException ex) {
            throw new LambdaException("Failed getting CtClass for [%s] as it is not found", inputClass, ex);
        }
    }

    private String toClassName(final Class inputClass) {
        if (inputClass.isArray())
            return toClassName(inputClass.getComponentType()) + "[]";

        return inputClass.getName();
    }

}
