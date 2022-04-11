package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.sigh.types.Type;
import norswap.utils.Util;

public final class ArrayTypeNode extends TypeNode
{
    public TypeNode componentType;

    public ArrayTypeNode (Span span, Object componentType) {
        super(span);
        this.componentType = Util.cast(componentType, TypeNode.class);
    }

    // TODO
    @Override
    public Type getType () {
        return null;
    }

    @Override public String contents() {
        return componentType.contents() + "[]";
    }
}
