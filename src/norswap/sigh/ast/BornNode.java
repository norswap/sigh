package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class BornNode extends ExpressionNode
{
    public final ReferenceNode reference;
    public final BlockNode block;

    public BornNode (Span span, Object reference, Object block) {
        super(span);
        this.reference = Util.cast(reference, ReferenceNode.class);
        this.block = Util.cast(block, BlockNode.class);
    }

    @Override public String contents ()
    {
        return reference.name;
    }
}
