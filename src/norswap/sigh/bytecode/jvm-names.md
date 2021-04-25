# Java & JVM Names

Java and JVM names are a bit tricky to understand, here is a document to disambiguate.

## TODO

- qualified name

## Simple Names
- https://docs.oracle.com/javase/specs/jls/se8/html/jls-6.html#jls-6.2

In theory, only things that are explicitly declared with a name have a simple name, and the simple
name is a single identifier, like `int`, `String`.

However, there is a `Class#getSimpleName()` method, which provides the following results:

- `int`: `int`
- `String: `String`
- `int: `int`
- `MyClass.InnerClass`: `InnerClass`
- `MyClass.StaticNestedClass`: `StaticNestedClass`
- `MethodLocalClass`: `MethodLocalClass`
- anonymous class (e.g. `new AbstractClass() {}`): empty!
- lambda for  Runnable (e.g. `() -> {}`): `MyClass$$Lambda$1/1418481495`
- arrays: simple name of the component type followed by `[]`

## Canonical Names
- https://docs.oracle.com/javase/specs/jls/se8/html/jls-6.html#jls-6.7

They include the full package path (dot-separated) and separate the name of inner and
static nested classes by a dot. Method-local classes don't have such a name, nor do
any anonymous types (including lambdas). Types derived from types that do not have
a canonical name (array types, nested types) also do not have a canonical name.

You can get the canonical name of a method using `Class#getCanonicalName()`:

- `String`: `java.util.String`
- `MyClass.InnerClass`: `package.path.MyClass.MyClassInnerClass`
- `MyClass.StaticNestedClass`: `package.path.MyClass.StaticNestedClass`
- `MethodLocalClass`: `null`
- anonymous class (e.g. `new AbstractClass() {}`): `null` (the object, not the string `"null"`)
- lambda for `Runnable` (e.g. `() -> {`}: `package.path.MyClass$$Lambda$1/1418481495`
- arrays: canonical name of the component followed by `[]` (or `null`)

Note how the returned canonical names for lambdas contradict the specification!

## Fully-Qualified Names

Fully-qualified are similar to canonical name. In fact a canonical name is always a fully-qualified
name. The difference is that a type can have **multiple** fully-qualified type names via
inheritance.

If `C2` extends `C1` `C1` has inner class `C3` then `C1.C3` and `C2.C3` are fully-qualified package
names for `C3` (assuming `C1` and `C2` live in the top-level package). In this case, `C1.C3` is the
canonical name.

## Binary Names
- https://docs.oracle.com/javase/specs/jls/se8/html/jls-13.html#jls-13.1

Binary names are only defined for class names (and assimilated: interfaces, enums, etc). They are
identical to canonical names for top-level classes, however they vary in how they separate inner
and static nested classes (with a dollar sign) and are able to represent anonymous classes.

For types that do have a binary name, you can retrieve it using `Class#getName()`. 

- package-level classes: same as canonical name
- `MyClass.InnerClass`: `package.path.MyClass$MyClassInnerClass`
- `MyClass.StaticNestedClass`: `package.path.MyClass$StaticNestedClass`
- `MethodLocalClass`: `package.path.MyClass$1MethodLocalClass`
- anonymous class (e.g. `new AbstractClass() {}`): `package.path.MyClass$1`
- lambda for `Runnable` (e.g. `() -> {}`): `package.path.MyClass$$Lambda$1/1418481495`

**!!** For things that do not have a binary name (primitives, arrays), `Class#getName()` returns the
*field descriptor (see below).

**TODO** : type variables introduced by classes, methods and constructors also have a binary name

## Field and Method Descriptors
- https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.3

These descriptors are used to encode types as String in the JVM. Field descriptors are used
for values (and interestingly, you only need to store this for fields, hence the name).

Method descriptors combine field descriptors to represent the return and parameter types of a
method.

Here is how these descriptors are formed:

- `boolean`: `Z`
- `byte`: `B`
- `char`: `C`
- `short`: `S`
- `int`: `I`
- `long`: `J`
- `float`: `F`
- `double`: `D`
- `void`: `V`
- class: `L<slash-separated-binary-name>;`
    - e.g. `Ljava/lang/String;`, `Lpackage/path/MyClass$MyClassInnerClass;`
- array: `[<component-field-descriptor>`
    - e.g. `[I`, `[[I`, `[Ljava/lang/String;`
- methods: `(<parameter-type-field-descriptor-concatenated>)<return-type-field-descriptor>`
    - e.g. `(IJ[Ljava/lang/String;)V` for `void test (int x, long y, String[] z)`

For constructors `<init>` is used as the method name, and void as the method name and
`void` (descriptor `V`) as the return type.

## Type Signatures
- https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.9.1

Type signatures extend field & method signatures to support generic types (including type parameters
& type bounds) as well as thrown checked exceptions for methods.

TODO: document