package norswap.sigh.ast;

import norswap.autumn.positions.Span;

public final class IntLiteralNode extends ExpressionNode
{
    public final long value;

    public IntLiteralNode (Span span, long value) {
        super(span);
        this.value = value;
    }

    @Override public String contents() {
        return String.valueOf(value);
    }
}
