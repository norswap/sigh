package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class SimpleTypeNode extends TypeNode
{
    public final String name;

    public SimpleTypeNode (Span span, Object name) {
        super(span);
        this.name = Util.cast(name, String.class);
    }

    @Override public String contents () {
        return name;
    }
}
