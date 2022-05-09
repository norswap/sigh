package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public final class DotPrdExpression extends ExpressionNode
{
    public final ArrayLiteralNode left, right;


    public DotPrdExpression (Span span, Object left, Object right) {
        super(span);
        this.left =  Util.cast(left, ArrayLiteralNode.class);
        this.right = Util.cast(right, ArrayLiteralNode.class);

    }

    @Override public String contents ()
    {
        String candidate = String.format("@");
            //left.contents(), right.contents());

        return candidate;

    }
}

