package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public class AssignmentNode extends ExpressionNode
{
    public final ExpressionNode left;
    public final ExpressionNode right;

    public AssignmentNode (Span span, Object left, Object right) {
        super(span);
        this.left = Util.cast(left, ExpressionNode.class);
        this.right = Util.cast(right, ExpressionNode.class);
    }

    @Override public String contents ()
    {
        String leftEqual = left.contents() + " = ";

        String candidate = leftEqual + right.contents();
        if (candidate.length() <= contentsBudget())
            return candidate;

        candidate = leftEqual + "(?)";
        return candidate.length() <= contentsBudget()
            ? candidate
            : "(?) = (?)";
    }
}
