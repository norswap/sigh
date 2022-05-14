package norswap.sigh.types;

import norswap.sigh.ast.BoxDeclarationNode;

public class BoxType extends Type {
    public static final BoxType INSTANCE = new BoxType(null);
    public final BoxDeclarationNode node;

    public BoxType (BoxDeclarationNode node) {
        this.node = node;
    }

    @Override
    public String name () {
        return node.name();
    }

    @Override
    public boolean equals (Object o) {
        return this == o || o instanceof BoxType && this.node == ((BoxType) o).node;
    }

    @Override
    public int hashCode () {
        return node.hashCode();
    }
}