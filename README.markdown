# The Lambda Experiment

The Lambda Experiment is inspired by a number of sources - the proposals for Java 8 Lambdas, The Scala programming language
and some projects trying to do things like Lambdas for Java (e.g. [LambdaJ](http://code.google.com/p/lambdaj/) ).

The Lambda Experiment tries to implement Lambdas for the Java programming language, within the constraints of Java 6 & 7.
It is built as a prof of concept and as such, some features were not completed.

## What The Lambda Experiment does

The Lambda Experiment project enables writing functional style code in Java. The simple list.map operation can be done as

```java
aList.map(Integer.class, "a*a");
```

Which is equivalent to the Scala code

```scala
aList.map(_*_)
```

We have implemented three examples of the List map operation. All three perform the same operation -
returns a new list of integers with the square of each element of the original list.

```java
aList.map(Integer.class, "a*a");
aList.mapTo(Integer.class).with("a*a");
aList.map(Lambda(Integer.class, var(Integer.class)).build("a*a"));
```

The Lambda Experiment also supports binding variables from the enclosing scope such as

```java
int x = 6;
aList.map(Integer.class, "a*a+b", val(x));
aList.map(Integer.class, "a*a+x", val("x", x));
```

Which is equivalent to the Scala code

```scala
val x = 6
aList.map(_*_+x)
```


In addition, The Lambda Experiment supports SAM (Single Abstract Method) Interfaces, which enables reusing existing Java interfaces
that take SAM parameters as inputs. A Good example is the Collections.sort operation which accepts a Comparator<?> SAM.

```java
Collections.sort(aList, Lambda(Comparator.class, Float.class).build("(a>b?1:(a<b?-1:0))"));
Collections.sort(aList, Lambda(Comparator.class, Integer.class).build("a-b"));
```

or with a simple factory

```java
@Factory
private <T> Comparator<T> Comparator(Class<T> ofType, String code) {
    //noinspection unchecked
    return Lambda(Comparator.class, ofType).build(code);
}

Collections.sort(aList, Comparator(Integer.class, "(a>b?1:(a<b?-1:0))"));
```

## How is the Lambda Experiment implemented

The core of the Lambda Experiment is the Lambdas class which exposes the static methods for Lambda creation.
It offers two styles of Lambda methods - for the creation of SAM Lambdas and the creation of FuctionN Lambdas.

The methods for creation of FunctionN Lambdas

```java
public static <R,T> LambdaSignature<Function1<R,T>> Lambda(Class<R> retType, Var<T> var1)
public static <R,T1, T2> LambdaSignature<Function2<R,T1, T2>> Lambda(Class<R> retType, Var<T1> var1, Var<T2> var2)
public static <R,T1, T2, T3> LambdaSignature<Function3<R,T1, T2, T3>> Lambda(Class<R> retType, Var<T1> var1, Var<T2> var2, Var<T3> var3)
```

The methods for creation of SAM Lambdas

```java
public static <SAM> SAMSignature<SAM> Lambda(Class<SAM> samType)
public static <SAM> SAMSignature<SAM> Lambda(Class<SAM> samType, Class<?> ... genericTypes)
```

The LambdaSignature<F> class is a factory class that, given a code and a set of variable bindings, will generate a class
implementing the F interface. The actual code generation is done using javassist.

For example, building the following Lambda

```java
Lambda(Integer.class, var(Integer.class), var(Integer.class)).build("a+b+c", val(12));
```

will generate the following class
```java
class Lambda$$1340129336 implements org.wixpress.hoopoe.lambda.Function2 {
	int c;
	public Lambda$$1340129336(java.lang.Integer c) {
		this.c = ((Integer)c).intValue();
	}
	public Object apply(Object a, Object b) {
		return new Integer(invokeInternal(((Integer)a).intValue(), ((Integer)b).intValue()));
	}
	int invokeInternal(int a, int b) {
		return a+b+c;
	}
	public Class retType() {
		return java.lang.Integer.class;
	}
	public Class[] varTypes() {
		return new Class[] {java.lang.Integer.class, java.lang.Integer.class};
	}
}
```

The SAMSignature<SAM> class is a factory class, that given a code and a set of variable bindings, will generate a
Lambda class using the LambdaSignature<F> (where the actual signature is read from the SAM interface) and will wrap the
resulting class with a JDK Proxy implementing the SAM interface.


## What the Lambda Experiment does not do

The Lambda Experiment was built as a prof of concept and as such, we did not invest in proving all the features required
by a production library. It is important to note that the Lambda Experiment is built with the quality of production code
in mind (including performance and testing support). Having said that, the following is the list of features not implemented
yet.

+ Multiple line expressions. The Lambda Experiment assumes that the string expressions passed to the build method
  are single line expressions, that are always wrapped with "return {expression};".
+ Functions with over 3 parameters. It is trivial to add those - not interesting for experiment scope.
+ Curry operations - transform a Function3 into Function2 by providing a variable value. This seems trivial and as such,
  was not implemented.
+ Functional Collections - the project does not implement another set of functional collection libraries for Java. We think
  there are enough collection libraries as it is - our preferred approach is for one of those libraries to take the ideas of
  The Lambda Project and incorporate them.

