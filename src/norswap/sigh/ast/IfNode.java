package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class IfNode extends StatementNode
{
    public final ExpressionNode condition;
    public final StatementNode trueStatement;
    public final StatementNode falseStatement;

    public IfNode (Span span, Object condition, Object trueStatement, Object falseStatement) {
        super(span);
        this.condition = Util.cast(condition, ExpressionNode.class);
        this.trueStatement = Util.cast(trueStatement, StatementNode.class);
        this.falseStatement = falseStatement == null
            ? null
            : Util.cast(falseStatement, StatementNode.class);
    }

    @Override public String contents ()
    {
        String condition = this.condition.contents();
        String candidate = falseStatement == null
            ? String.format("if %s ...", condition)
            : String.format("if %s ... else ...", condition);

        return candidate.length() <= contentsBudget()
            ? candidate
            : falseStatement == null
                ? "if (?) ..."
                : "if (?) ... else ...";
    }
}
