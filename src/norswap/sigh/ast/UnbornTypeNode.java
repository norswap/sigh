package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class UnbornTypeNode extends TypeNode
{
    public final TypeNode componentType;

    public UnbornTypeNode (Span span, Object componentType) {
        super(span);
        this.componentType = Util.cast(componentType, TypeNode.class);
    }

    @Override public String contents() {
        return "Unborn<" + componentType.contents() + ">";
    }
}

