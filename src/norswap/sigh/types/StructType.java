package norswap.sigh.types;

import norswap.sigh.ast.StructDeclarationNode;

public final class StructType extends Type
{
    public final StructDeclarationNode node;

    public StructType (StructDeclarationNode node) {
        this.node = node;
    }

    @Override public String name() {
        return node.name();
    }

    @Override public boolean equals (Object o) {
        return this == o || o instanceof StructType && this.node == ((StructType) o).node;
    }

    @Override public int hashCode () {
        return node.hashCode();
    }
}
