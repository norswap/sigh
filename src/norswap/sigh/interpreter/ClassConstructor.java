package norswap.sigh.interpreter;

import norswap.sigh.ast.ClassDeclarationNode;

/**
 * Class representing class constructors in the interpreter, simply wrapping the declaration
 * node. Such a wrapper is necessary, because the node is already used to represent the class
 * type.
 */
public final class ClassConstructor
{
    public final ClassDeclarationNode declaration;

    public ClassConstructor (ClassDeclarationNode declaration) {
        this.declaration = declaration;
    }

    @Override public int hashCode () {
        return 31 * declaration.hashCode() + 1;
    }

    @Override public boolean equals (Object other) {
        return other instanceof ClassConstructor && ((ClassConstructor) other).declaration == declaration;
    }
}
