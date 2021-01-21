package norswap.sigh.interpreter;

import norswap.sigh.ast.StructDeclarationNode;

/**
 * Class representing structure constructors in the interpreter, simply wrapping the declaration
 * node. Such a wrapper is necessary, because the node is already used to represent the structure
 * type.
 */
public final class Constructor
{
    public final StructDeclarationNode declaration;

    public Constructor (StructDeclarationNode declaration) {
        this.declaration = declaration;
    }

    @Override public int hashCode () {
        return 31 * declaration.hashCode() + 1;
    }

    @Override public boolean equals (Object other) {
        return other instanceof Constructor && ((Constructor) other).declaration == declaration;
    }
}
