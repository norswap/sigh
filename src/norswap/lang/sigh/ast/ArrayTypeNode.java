package norswap.lang.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ArrayTypeNode extends TypeNode
{
    public final TypeNode component_type;

    public ArrayTypeNode (Span span, Object component_type) {
        super(span);
        this.component_type = Util.cast(component_type, TypeNode.class);
    }

    @Override public String contents() {
        return component_type.contents() + "[]";
    }
}
