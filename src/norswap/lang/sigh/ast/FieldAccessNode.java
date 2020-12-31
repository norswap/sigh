package norswap.lang.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class FieldAccessNode extends ExpressionNode
{
    public final ExpressionNode stem;
    public final String field_name;

    public FieldAccessNode (Span span, Object stem, Object field_name) {
        super(span);
        this.stem = Util.cast(stem, ExpressionNode.class);
        this.field_name = Util.cast(field_name, String.class);
    }

    @Override public String contents ()
    {
        String candidate = String.format("%s.%s", stem.contents(), field_name);
        return candidate.length() <= contents_budget()
            ? candidate
            : "(?)." + field_name;
    }
}
