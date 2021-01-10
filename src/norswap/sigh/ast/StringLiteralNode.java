package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class StringLiteralNode extends ExpressionNode
{
    public final String value;

    public StringLiteralNode (Span span, Object value) {
        super(span);
        this.value = Util.cast(value, String.class);
    }

    @Override public String contents () {
        return String.format("\"%s\"", value);
    }
}
