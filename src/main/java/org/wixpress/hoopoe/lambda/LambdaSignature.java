package org.wixpress.hoopoe.lambda;

import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Yoav
 * @since 10/3/11
 */
public class LambdaSignature<F> {

    private static AtomicInteger lambdaCounter = new AtomicInteger();
    private RetType retType;
    private Var<?>[] vars;
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
        LambdaGen lambdaGen = generateClass(code, vals);
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

    private LambdaGen generateClass(String code, Val[] vals) {
        LambdaGen lambdaGen = new LambdaGen();
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

    private class LambdaGen {
        String invokeCode;
        String invokeInternalCode;
        String[] fieldsCode;
        String lambdaName;
        String constructorCode;
        Class<?> functionInterface;
        Class<F> lambdaClass;

        public String toString() {
            StringBuilder sb = new StringBuilder().append("class ").append(lambdaName).append(" implements ").append(functionInterface.getName()).append(" {\n");
            for (String fieldCode: fieldsCode)
                sb.append("\t").append(fieldCode).append(";\n");
            sb.append(constructorCode).append("\n");
            sb.append(invokeCode).append("\n");
            sb.append(invokeInternalCode).append("\n");
            sb.append("}");
            return sb.toString();
        }

        public void generateClassCode(String code, Val[] vals) {
            this.invokeCode = writeInvokeCode();
            this.invokeInternalCode = writeInvokeInternalCode(code);
            this.fieldsCode = writeVariablesCode(vals);
            this.lambdaName = generateLambdaName();
            this.constructorCode = writeConstructorCode(lambdaName, vals);
            this.functionInterface = getFunctionInterface();
        }
    }

    private String writeConstructorCode(String lambdaName, Val[] vals) {
        if (vals.length >0 ) {
            StringBuilder sb = new StringBuilder();
            sb.append("\tpublic ").append(lambdaName).append("(");
            for (int i=0; i < vals.length; i++) {
                Val val = vals[i];
                sb.append(val.boxedType().getName()).append(" ").append(val.getName()).append((i==vals.length-1)?") {\n":", ");
            }
            for (Val val : vals) {
                sb.append("\t\tthis.").append(val.getName()).append(" = ").append(val.unboxCode(val.getName())).append(";\n");
            }
            sb.append("\t}");
            return sb.toString();
        }
        else
            return null;
    }

    private String[] writeVariablesCode(Val[] vals) {
        String[] valsCode = new String[vals.length];
        for (int i=0; i < vals.length; i++)
            valsCode[i] = String.format("%s %s;", vals[i].primitiveType(), vals[i].getName());
        return valsCode;
    }

    private String writeInvokeCode() {
        StringBuilder methodCode = new StringBuilder();
        methodCode.append("\tpublic Object apply(");
        for (int i = 0, varsLength = vars.length; i < varsLength; i++) {
            Var<?> var = vars[i];
            methodCode.append("Object ").append(var.getName()).append((i==varsLength-1)?") {\n":", ");
        }
        methodCode.append("\t\treturn ").append(retType.boxCode(formatCallToInvokeInternal())).append(";\n\t}");

        return methodCode.toString();
    }

    private String formatCallToInvokeInternal() {
        StringBuilder invokeInternalParameters = new StringBuilder();
        for (int i = 0, varsLength = vars.length; i < varsLength; i++) {
            Var<?> var = vars[i];
            invokeInternalParameters.append(var.unboxCode(var.getName())).append((i==varsLength-1)?"":", ");
        }
        return String.format("invokeInternal(%s)", invokeInternalParameters.toString());
    }

    private String writeInvokeInternalCode(String code) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t").append(retType.primitiveType().getName()).append(" invokeInternal(");
        for (int i = 0, varsLength = vars.length; i < varsLength; i++) {
            Var<?> var = vars[i];
            sb.append(var.primitiveType().getName()).append(" ").append(var.getName()).append((i==varsLength-1)?") {\n":", ");
        }
        sb.append("\t\treturn ").append(code).append(";\n\t}");
        return sb.toString();
    }

    private String generateLambdaName() {
        return String.format("Lambda$$%d", lambdaCounter.getAndIncrement());
    }

    private Class getFunctionInterface() {
        switch (vars.length) {
            case 1: return Function1.class;
            case 2: return Function2.class;
            case 3: return Function3.class;
            default: throw new LambdaException("unsupported number of lambda parameters [%d]", vars.length);
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
