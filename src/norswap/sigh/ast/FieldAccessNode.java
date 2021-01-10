package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class FieldAccessNode extends ExpressionNode
{
    public final ExpressionNode stem;
    public final String fieldName;

    public FieldAccessNode (Span span, Object stem, Object fieldName) {
        super(span);
        this.stem = Util.cast(stem, ExpressionNode.class);
        this.fieldName = Util.cast(fieldName, String.class);
    }

    @Override public String contents ()
    {
        String candidate = String.format("%s.%s", stem.contents(), fieldName);
        return candidate.length() <= contentsBudget()
            ? candidate
            : "(?)." + fieldName;
    }
}
