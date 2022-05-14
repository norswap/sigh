package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public class AttributeAccessNode extends ExpressionNode
{
    public final ExpressionNode stem;
    public final String attributeName;

    public AttributeAccessNode (Span span, Object stem, Object attributeName) {
        super(span);
        this.stem = Util.cast(stem, ExpressionNode.class);
        this.attributeName = Util.cast(attributeName, String.class);
    }

    @Override public String contents ()
    {
        String candidate = String.format("%s#%s", stem.contents(), attributeName);
        return candidate.length() <= contentsBudget()
            ? candidate
            : "(?)#" + attributeName;
    }
}
