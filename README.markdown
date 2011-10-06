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

```scala
aList.map(_*_)
```


All three perform the same operation - returns a new list of integers with the square of each element of the original list.

In addition, The Lambda Experiment supports SAM (Single Abstract Method) Interfaces, which enables reusing existing SAM interfaces

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

