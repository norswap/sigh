package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public final class FunCallNode extends ExpressionNode
{
    public final ExpressionNode function;
    public final List<ExpressionNode> arguments;

    @SuppressWarnings("unchecked")
    public FunCallNode (Span span, Object function, Object arguments) {
        super(span);
        this.function = Util.cast(function, ExpressionNode.class);
        this.arguments = Util.cast(arguments, List.class);
    }

    @Override public String contents ()
    {
        String args = arguments.size() == 0 ? "()" : "(...)";
        return function.contents() + args;
    }
}
