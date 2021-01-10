package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ReferenceNode extends ExpressionNode
{
    public final String name;

    public ReferenceNode (Span span, Object name) {
        super(span);
        this.name = Util.cast(name, String.class);
    }

    @Override public String contents() {
        return name;
    }
}
