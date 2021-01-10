package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ParenthesizedNode extends ExpressionNode
{
    public final ExpressionNode expression;

    public ParenthesizedNode (Span span, Object expression) {
        super(span);
        this.expression = Util.cast(expression, ExpressionNode.class);
    }

    @Override public String contents() {
        return String.format("(%s)", expression.contents());
    }
}
