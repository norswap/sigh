package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public class BoxConstructorNode extends ExpressionNode
{
    public final ReferenceNode ref;

    public BoxConstructorNode (Span span, Object ref) {
        super(span);
        this.ref = Util.cast(ref, ReferenceNode.class);
    }

    @Override public String contents () {
        return "create " + ref.name;
    }
}
