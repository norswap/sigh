package norswap.lang.sigh.typing;

import norswap.lang.sigh.ast.Node;
import norswap.lang.sigh.typing.hierarchy.*;
import norswap.uranium.Reactor;

import static norswap.lang.sigh.typing.SyntheticDeclaration.Kind.*;

public class RootScope extends Scope
{
    private SyntheticDeclaration decl (String name, SyntheticDeclaration.Kind kind) {
        SyntheticDeclaration decl = new SyntheticDeclaration(name, kind);
        declare(name,  decl);
        return decl;
    }

    // root scope types
    private final SyntheticDeclaration Bool   = decl("Bool",   TYPE);
    private final SyntheticDeclaration Int    = decl("Int",    TYPE);
    private final SyntheticDeclaration Float  = decl("Float",  TYPE);
    private final SyntheticDeclaration String = decl("String", TYPE);
    private final SyntheticDeclaration Void   = decl("Void",   TYPE);

    // root scope functions
    private final SyntheticDeclaration print = decl("print", FUNCTION);

    // root scope variables
    private final SyntheticDeclaration _true  = decl("true", VARIABLE);
    private final SyntheticDeclaration _false = decl("false", VARIABLE);
    private final SyntheticDeclaration _null  = decl("null", VARIABLE);

    RootScope (Node node) {
        super(node, null);
    }

    void initialize (Reactor R)
    {
        R.set(Bool,   "type",   BoolType.INSTANCE);
        R.set(Int,    "type",    IntType.INSTANCE);
        R.set(Float,  "type",  FloatType.INSTANCE);
        R.set(String, "type", StringType.INSTANCE);
        R.set(Void,   "type",   VoidType.INSTANCE);

        R.set(print,  "type",   new FunType(StringType.INSTANCE, StringType.INSTANCE));

        R.set(_true,  "type",   BoolType.INSTANCE);
        R.set(_false, "type",   BoolType.INSTANCE);
        R.set(_null,  "type",   NullType.INSTANCE);
    }
}
