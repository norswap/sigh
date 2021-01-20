package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public class ReturnNode extends StatementNode
{
    public final ExpressionNode expression;

    public ReturnNode (Span span, Object expression) {
        super(span);
        this.expression = expression == null
            ? null
            : Util.cast(expression, ExpressionNode.class);
    }

    @Override public String contents () {
        return "return " + (expression == null ? "" : expression.contents());
    }
}
