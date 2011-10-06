package org.wixpress.hoopoe.lambda;

import java.util.concurrent.atomic.AtomicInteger;

/**
* @author Yoav
* @since 10/6/11
*/
class LambdaCodeGenerator<F> {
    private static AtomicInteger lambdaCounter = new AtomicInteger();
    String invokeCode;
    String invokeInternalCode;
    String[] fieldsCode;
    String lambdaName;
    String constructorCode;
    Class<?> functionInterface;
    Class<F> lambdaClass;
    private LambdaSignature<F> lambdaSignature;

    public LambdaCodeGenerator(LambdaSignature<F> lambdaSignature) {
        this.lambdaSignature = lambdaSignature;
    }

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
        for (int i = 0, varsLength = lambdaSignature.vars.length; i < varsLength; i++) {
            Var<?> var = lambdaSignature.vars[i];
            methodCode.append("Object ").append(var.getName()).append((i==varsLength-1)?") {\n":", ");
        }
        methodCode.append("\t\treturn ").append(lambdaSignature.retType.boxCode(formatCallToInvokeInternal())).append(";\n\t}");

        return methodCode.toString();
    }

    private String formatCallToInvokeInternal() {
        StringBuilder invokeInternalParameters = new StringBuilder();
        for (int i = 0, varsLength = lambdaSignature.vars.length; i < varsLength; i++) {
            Var<?> var = lambdaSignature.vars[i];
            invokeInternalParameters.append(var.unboxCode(var.getName())).append((i==varsLength-1)?"":", ");
        }
        return String.format("invokeInternal(%s)", invokeInternalParameters.toString());
    }

    private String writeInvokeInternalCode(String code) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t").append(lambdaSignature.retType.primitiveType().getName()).append(" invokeInternal(");
        for (int i = 0, varsLength = lambdaSignature.vars.length; i < varsLength; i++) {
            Var<?> var = lambdaSignature.vars[i];
            sb.append(var.primitiveType().getName()).append(" ").append(var.getName()).append((i==varsLength-1)?") {\n":", ");
        }
        sb.append("\t\treturn ").append(code).append(";\n\t}");
        return sb.toString();
    }

    private String generateLambdaName() {
        return String.format("Lambda$$%d", lambdaCounter.getAndIncrement());
    }

    private Class getFunctionInterface() {
        switch (lambdaSignature.vars.length) {
            case 1: return Function1.class;
            case 2: return Function2.class;
            case 3: return Function3.class;
            default: throw new LambdaException("unsupported number of lambda parameters [%d]", lambdaSignature.vars.length);
        }
    }


}
