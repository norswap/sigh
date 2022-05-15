package norswap.sigh.types;

import norswap.sigh.ast.ArrayDeclarationNode;
import java.util.ArrayList;
import java.util.List;

public final class ArrayType extends Type
{
    public final Type componentType;
    public static final ArrayType INSTANCE = new ArrayType(VoidType.INSTANCE,null);
    public List dimensions;

    public ArrayType (Type componentType, List dimensions) {
        this.componentType = componentType;
        this.dimensions=dimensions;
    }

    @Override public String name() {
        return componentType.toString() + "[]";
    }

    @Override public boolean equals (Object o) {
        return this == o || o instanceof ArrayType && componentType.equals(o);
    }

    @Override public int hashCode () {
        return componentType.hashCode();
    }

}
