package norswap.sigh.interpreter;

import norswap.sigh.ast.BoxDeclarationNode;

/**
 * Class representing box constructors in the interpreter, simply wrapping the declaration
 * node. Such a wrapper is necessary, because the node is already used to represent the box
 * type.
 */
public class BoxConstructor
{
    public final BoxDeclarationNode declaration;

    public BoxConstructor (BoxDeclarationNode declaration) {
        this.declaration = declaration;
    }

    @Override public int hashCode () {
        return 31 * declaration.hashCode() + 1;
    }

    @Override public boolean equals (Object other) {
        return other instanceof BoxConstructor && ((BoxConstructor) other).declaration == declaration;
    }
}
