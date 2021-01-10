package norswap.lang.sigh.scopes;

import norswap.lang.sigh.ast.Node;
import norswap.lang.sigh.types.*;
import norswap.uranium.Reactor;

import static norswap.lang.sigh.scopes.SyntheticDeclarationNode.Kind.*;

public class RootScope extends Scope
{
    private SyntheticDeclarationNode decl (String name, SyntheticDeclarationNode.Kind kind) {
        SyntheticDeclarationNode decl = new SyntheticDeclarationNode(name, kind);
        declare(name,  decl);
        return decl;
    }

    // root scope types
    private final SyntheticDeclarationNode Bool   = decl("Bool",   TYPE);
    private final SyntheticDeclarationNode Int    = decl("Int",    TYPE);
    private final SyntheticDeclarationNode Float  = decl("Float",  TYPE);
    private final SyntheticDeclarationNode String = decl("String", TYPE);
    private final SyntheticDeclarationNode Void   = decl("Void",   TYPE);

    // root scope variables
    private final SyntheticDeclarationNode _true  = decl("true",  VARIABLE);
    private final SyntheticDeclarationNode _false = decl("false", VARIABLE);
    private final SyntheticDeclarationNode _null  = decl("null",  VARIABLE);

    // root scope functions
    private final SyntheticDeclarationNode print = decl("print", FUNCTION);

    public RootScope (Node node) {
        super(node, null);
    }

    public void initialize (Reactor R)
    {
        R.set(Bool,   "type",   BoolType.INSTANCE);
        R.set(Int,    "type",    IntType.INSTANCE);
        R.set(Float,  "type",  FloatType.INSTANCE);
        R.set(String, "type", StringType.INSTANCE);
        R.set(Void,   "type",   VoidType.INSTANCE);

        R.set(_true,  "type",   BoolType.INSTANCE);
        R.set(_false, "type",   BoolType.INSTANCE);
        R.set(_null,  "type",   NullType.INSTANCE);

        R.set(print,  "type", new FunType(StringType.INSTANCE, StringType.INSTANCE));
    }
}
