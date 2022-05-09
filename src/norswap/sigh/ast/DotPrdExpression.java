package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class DotPrdExpression extends ExpressionNode
{
    public final ExpressionNode left, right;


    public DotPrdExpression (Span span, Object left, Object right) {
        super(span);
        this.left = Util.cast(left, ExpressionNode.class);
        this.right = Util.cast(right, ExpressionNode.class);

    }

    @Override public String contents ()
    {
        String candidate = String.format("%s @ %s",
            left.contents(), right.contents());

        return candidate;

    }
}

