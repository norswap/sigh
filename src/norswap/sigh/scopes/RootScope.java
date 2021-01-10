package norswap.sigh.scopes;

import norswap.sigh.ast.RootNode;
import norswap.sigh.types.*;
import norswap.uranium.Reactor;

import static norswap.sigh.scopes.DeclarationKind.*;

/**
 * The lexical scope of a file in Sigh. It is notably responsible for introducing the default
 * declarations made by the language.
 */
public final class RootScope extends Scope
{
    // ---------------------------------------------------------------------------------------------

    private SyntheticDeclarationNode decl (String name, DeclarationKind kind) {
        SyntheticDeclarationNode decl = new SyntheticDeclarationNode(name, kind);
        declare(name,  decl);
        return decl;
    }

    // ---------------------------------------------------------------------------------------------

    // root scope types
    public final SyntheticDeclarationNode Bool   = decl("Bool",   TYPE);
    public final SyntheticDeclarationNode Int    = decl("Int",    TYPE);
    public final SyntheticDeclarationNode Float  = decl("Float",  TYPE);
    public final SyntheticDeclarationNode String = decl("String", TYPE);
    public final SyntheticDeclarationNode Void   = decl("Void",   TYPE);

    // root scope variables
    public final SyntheticDeclarationNode _true  = decl("true",  VARIABLE);
    public final SyntheticDeclarationNode _false = decl("false", VARIABLE);
    public final SyntheticDeclarationNode _null  = decl("null",  VARIABLE);

    // root scope functions
    public final SyntheticDeclarationNode print = decl("print", FUNCTION);

    // ---------------------------------------------------------------------------------------------

    public RootScope (RootNode node, Reactor reactor) {
        super(node, null);

        reactor.set(Bool,   "type",   BoolType.INSTANCE);
        reactor.set(Int,    "type",    IntType.INSTANCE);
        reactor.set(Float,  "type",  FloatType.INSTANCE);
        reactor.set(String, "type", StringType.INSTANCE);
        reactor.set(Void,   "type",   VoidType.INSTANCE);

        reactor.set(_true,  "type",   BoolType.INSTANCE);
        reactor.set(_false, "type",   BoolType.INSTANCE);
        reactor.set(_null,  "type",   NullType.INSTANCE);

        reactor.set(print,  "type", new FunType(StringType.INSTANCE, StringType.INSTANCE));
    }

    // ---------------------------------------------------------------------------------------------
}
