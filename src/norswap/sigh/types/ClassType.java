package norswap.sigh.types;

import norswap.sigh.ast.ClassDeclarationNode;

public final class ClassType extends Type
{
    public final ClassDeclarationNode node;

    public ClassType (ClassDeclarationNode node) {
        this.node = node;
    }

    @Override public String name() {
        return node.name();
    }

    @Override public boolean equals (Object o) {
        return this == o || o instanceof ClassType && this.node == ((ClassType) o).node;
    }

    @Override public int hashCode () {
        return node.hashCode();
    }
}
