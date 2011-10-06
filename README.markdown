# The Lambda Experiment

The Lambda Experiment is inspired by a number of sources - the proposals for Java 8 Lambdas, The Scala programming language
and some projects trying to do things like Lambdas for Java (e.g. [LambdaJ](http://code.google.com/p/lambdaj/) ).

The Lambda Experiment tries to implement Lambdas for the Java programming language, within the constraints of Java 6 & 7.
It is built as a prof of concept and as such, some features were not completed.

## What The Lambda Experiment does

The Lambda Experiment project enables writing functional style code in Java. We have implemented three examples of the List map operation

```java
aList.map(Integer.class, "a*a");
aList.mapTo(Integer.class).with("a*a");
aList.map(Lambda(Integer.class, var(Integer.class)).build("a*a"));
```

All three perform the same operation - returns a new list of integers with the square of each element of the original list.

In addition, The Lambda Experiment supports SAM (Single Abstract Method) Interfaces, which enables reusing existing SAM interfaces

```java
Collections.sort(aList, Lambda(Comparator.class, Integer.class).build("(a>b?1:(a<b?-1:0))"));
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


