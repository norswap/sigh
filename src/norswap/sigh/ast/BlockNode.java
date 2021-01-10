package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public final class BlockNode extends StatementNode
{
    public final List<StatementNode> statements;

    @SuppressWarnings("unchecked")
    public BlockNode (Span span, Object statements) {
        super(span);
        this.statements = Util.cast(statements, List.class);
    }

    @Override public String contents ()
    {
        if (statements.size() == 0)
            return "{}";

        String first = statements.get(0).contents();

        return first.length() <= contentsBudget() - "{  ... }".length()
            ? String.format("{ %s ... }", first)
            : "{ ... }";
    }
}
