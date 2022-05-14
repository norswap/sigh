package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public class BoxElementAccessNode extends ExpressionNode
{
    public final ExpressionNode stem;
    public final String elementName;

    public BoxElementAccessNode (Span span, Object stem, Object elementName) {
        super(span);
        this.stem = Util.cast(stem, ExpressionNode.class);
        this.elementName = Util.cast(elementName, String.class);
    }

    @Override public String contents ()
    {
        String candidate = String.format("%s#%s", stem.contents(), elementName);
        return candidate.length() <= contentsBudget()
            ? candidate
            : "(?)#" + elementName;
    }
}
